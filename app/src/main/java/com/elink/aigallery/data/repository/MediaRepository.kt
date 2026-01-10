package com.elink.aigallery.data.repository

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.provider.MediaStore
import com.elink.aigallery.data.db.AppDatabase
import com.elink.aigallery.data.db.FolderWithImages
import com.elink.aigallery.data.db.ImageTag
import com.elink.aigallery.data.db.MediaItem
import com.elink.aigallery.data.db.MediaTagAnalysis
import com.elink.aigallery.utils.MyLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

import androidx.room.withTransaction
import java.util.concurrent.TimeUnit

enum class ScanTrigger {
    FOREGROUND,
    OBSERVER
}

class MediaRepository(private val context: Context) {
    private val database = AppDatabase.getInstance(context)
    private val mediaDao = database.mediaDao()
    private val imageEmbeddingDao = database.imageEmbeddingDao()
    private val contentResolver: ContentResolver = context.contentResolver
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val scanMutex = Mutex()

    fun observeFolders(): Flow<List<FolderWithImages>> = mediaDao.getImagesByFolder()

    // --- Semantic Search & Embedding ---

    suspend fun getImagesWithoutEmbedding(limit: Int): List<MediaItem> {
        return mediaDao.getImagesWithoutEmbedding(limit)
    }

    suspend fun insertEmbedding(mediaId: Long, embedding: FloatArray) {
        // Convert FloatArray to ByteArray for storage
        val buffer = java.nio.ByteBuffer.allocate(embedding.size * 4)
        buffer.asFloatBuffer().put(embedding)
        val bytes = buffer.array()
        
        val entity = com.elink.aigallery.data.db.ImageEmbedding(
            mediaId = mediaId,
            embedding = bytes,
            embeddingDim = embedding.size
        )
        imageEmbeddingDao.insert(entity)
    }
    
    // Fixed implementation
    suspend fun semanticSearch(text: String, limit: Int = 50): List<MediaItem> {
        val clipHelper = com.elink.aigallery.ai.ClipHelper.getInstance(context)
        val textEmbedding = clipHelper.embedText(text) ?: return emptyList()

        val allEmbeddings = imageEmbeddingDao.getAllEmbeddings()
        if (allEmbeddings.isEmpty()) return emptyList()

        // Compute similarities
        val scores = allEmbeddings.map { entity ->
            val floatBuffer = java.nio.ByteBuffer.wrap(entity.embedding).asFloatBuffer()
            val imgVector = FloatArray(entity.embeddingDim)
            floatBuffer.get(imgVector)
            
            val score = dotProduct(textEmbedding, imgVector)
            Pair(entity.mediaId, score)
        }

        // Sort by score descending
        val topIds = scores.sortedByDescending { it.second }
            .take(limit)
            .map { it.first }

        if (topIds.isEmpty()) return emptyList()

        // Fetch MediaItems (preserving order is tricky with SQL IN, so we re-sort in memory)
        val items = mediaDao.getMediaItemsByIds(topIds)
        val itemMap = items.associateBy { it.id }
        
        return topIds.mapNotNull { itemMap[it] }
    }

    private fun dotProduct(v1: FloatArray, v2: FloatArray): Float {
        var sum = 0.0f
        for (i in v1.indices) {
            sum += v1[i] * v2[i]
        }
        return sum
    }

    // -----------------------------------

    suspend fun scanMedia(trigger: ScanTrigger) {
        scanMutex.withLock {
            val now = System.currentTimeMillis()
            val lastScanAt = prefs.getLong(KEY_LAST_SCAN_AT, 0L)
            if (trigger == ScanTrigger.FOREGROUND && now - lastScanAt < MIN_SCAN_INTERVAL_MS) {
                MyLog.i(TAG, "Skip scan: trigger=$trigger lastScanAt=$lastScanAt")
                return
            }

            val scanStartedAt = now
            val lastFullScanAt = prefs.getLong(KEY_LAST_FULL_SCAN_AT, 0L)
            val needsFullScan = lastFullScanAt == 0L ||
                scanStartedAt - lastFullScanAt >= FULL_SCAN_INTERVAL_MS

            if (needsFullScan) {
                MyLog.i(TAG, "Run full scan: trigger=$trigger lastFullScanAt=$lastFullScanAt")
                syncImagesInternal(null, null)
            } else {
                MyLog.i(TAG, "Run incremental scan: trigger=$trigger lastScanAt=$lastScanAt")
                syncImagesSince(lastScanAt)
            }

            val editor = prefs.edit().putLong(KEY_LAST_SCAN_AT, scanStartedAt)
            if (needsFullScan) {
                editor.putLong(KEY_LAST_FULL_SCAN_AT, scanStartedAt)
            }
            editor.apply()
        }
    }

    private suspend fun syncImagesSince(lastScanAt: Long) {
        val sinceMs = (lastScanAt - SCAN_OVERLAP_MS).coerceAtLeast(0L)
        val sinceSeconds = (sinceMs / 1000L).toString()
        val selection = """
            (${MediaStore.Images.Media.DATE_ADDED} > ? OR ${MediaStore.Images.Media.DATE_MODIFIED} > ? OR ${MediaStore.Images.Media.DATE_TAKEN} > ?)
        """.trimIndent()
        val selectionArgs = arrayOf(
            sinceSeconds,
            sinceSeconds,
            sinceMs.toString()
        )
        syncImagesInternal(selection, selectionArgs)
    }

    private suspend fun syncImagesInternal(
        selection: String?,
        selectionArgs: Array<String>?
    ) {
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME
        )

        val items = mutableListOf<MediaItem>()
        contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            "${MediaStore.Images.Media.DATE_TAKEN} DESC"
        )?.use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val pathIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            val dateIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
            val widthIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
            val heightIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)
            val folderIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)

            while (cursor.moveToNext()) {
                val mediaStoreId = cursor.getLong(idIndex)
                val path = cursor.getString(pathIndex) ?: continue
                val dateTaken = cursor.getLong(dateIndex)
                val width = cursor.getInt(widthIndex)
                val height = cursor.getInt(heightIndex)
                val folderName = cursor.getString(folderIndex) ?: "Unknown"

                items.add(
                    MediaItem(
                        mediaStoreId = mediaStoreId,
                        path = path,
                        dateTaken = dateTaken,
                        folderName = folderName,
                        width = width,
                        height = height
                    )
                )
            }
        }

        if (items.isNotEmpty()) {
            database.withTransaction {
                mediaDao.insertMediaItems(items)
                items.forEach {
                    mediaDao.updateMediaItemInfo(it.path, it.mediaStoreId, it.dateTaken, it.width, it.height)
                }
            }
        }
    }

    fun createDeleteRequest(mediaItems: List<MediaItem>): android.app.PendingIntent? {
        if (mediaItems.isEmpty()) return null
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU) {
            return null
        }
        val uris = mediaItems.map { item ->
            android.content.ContentUris.withAppendedId(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                item.mediaStoreId
            )
        }
        return MediaStore.createDeleteRequest(contentResolver, uris)
    }

    suspend fun deleteFromDb(path: String) {
        mediaDao.deleteMediaItemByPath(path)
    }

    suspend fun deleteFromDb(ids: List<Long>) {
        mediaDao.deleteMediaItems(ids)
    }

    suspend fun getMediaItemByPath(path: String): MediaItem? {
        return mediaDao.getMediaItemByPath(path)
    }

    fun searchImages(query: String, mappedQuery: String): Flow<List<MediaItem>> {
        return mediaDao.searchImages(query, mappedQuery)
    }

    fun observeImagesByLabel(label: String): Flow<List<MediaItem>> {
        return mediaDao.getImagesByLabel(label)
    }

    suspend fun getImagesWithoutTagAnalysis(limit: Int): List<MediaItem> {
        return mediaDao.getImagesWithoutTagAnalysis(limit)
    }

    suspend fun upsertMediaTagAnalysis(analysis: MediaTagAnalysis) {
        mediaDao.upsertMediaTagAnalysis(analysis)
    }

    suspend fun insertTags(mediaId: Long, labels: List<String>) {
        if (labels.isEmpty()) return
        val tags = labels.distinct().map { label ->
            ImageTag(mediaId = mediaId, label = label, confidence = 1.0f)
        }
        mediaDao.insertImageTags(tags)
    }

    suspend fun saveBitmapToGallery(
        bitmap: Bitmap,
        displayName: String,
        relativePath: String
    ): Boolean {
        val now = System.currentTimeMillis()
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Images.Media.MIME_TYPE, OUTPUT_MIME_TYPE)
            put(MediaStore.Images.Media.DATE_TAKEN, now)
            put(MediaStore.Images.Media.DATE_ADDED, now / 1000L)
            put(MediaStore.Images.Media.WIDTH, bitmap.width)
            put(MediaStore.Images.Media.HEIGHT, bitmap.height)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, relativePath)
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            ?: return false

        return try {
            val stream = contentResolver.openOutputStream(uri) ?: run {
                contentResolver.delete(uri, null, null)
                return false
            }
            stream.use { output ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, OUTPUT_QUALITY, output)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val pendingValues = ContentValues().apply {
                    put(MediaStore.Images.Media.IS_PENDING, 0)
                }
                contentResolver.update(uri, pendingValues, null, null)
            }
            true
        } catch (e: Exception) {
            MyLog.e(TAG, "Save bitmap failed", e)
            contentResolver.delete(uri, null, null)
            false
        }
    }

    companion object {
        private const val TAG = "MediaRepository"
        private const val PREFS_NAME = "media_scan_prefs"
        private const val KEY_LAST_SCAN_AT = "last_scan_at"
        private const val KEY_LAST_FULL_SCAN_AT = "last_full_scan_at"
        private const val MIN_SCAN_INTERVAL_MS = 3_000L
        private val FULL_SCAN_INTERVAL_MS = TimeUnit.HOURS.toMillis(24)
        private const val SCAN_OVERLAP_MS = 2_000L
        private const val OUTPUT_MIME_TYPE = "image/jpeg"
        private const val OUTPUT_QUALITY = 95
    }
}
