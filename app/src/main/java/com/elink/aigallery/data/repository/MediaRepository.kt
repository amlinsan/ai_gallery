package com.elink.aigallery.data.repository

import android.content.ContentResolver
import android.content.Context
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

class MediaRepository(context: Context) {
    private val database = AppDatabase.getInstance(context)
    private val mediaDao = database.mediaDao()
    private val contentResolver: ContentResolver = context.contentResolver
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val scanMutex = Mutex()

    fun observeFolders(): Flow<List<FolderWithImages>> = mediaDao.getImagesByFolder()

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

    companion object {
        private const val TAG = "MediaRepository"
        private const val PREFS_NAME = "media_scan_prefs"
        private const val KEY_LAST_SCAN_AT = "last_scan_at"
        private const val KEY_LAST_FULL_SCAN_AT = "last_full_scan_at"
        private const val MIN_SCAN_INTERVAL_MS = 3_000L
        private val FULL_SCAN_INTERVAL_MS = TimeUnit.HOURS.toMillis(24)
        private const val SCAN_OVERLAP_MS = 2_000L
    }
}
