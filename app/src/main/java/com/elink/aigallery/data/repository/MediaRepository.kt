package com.elink.aigallery.data.repository

import android.content.ContentResolver
import android.content.Context
import android.provider.MediaStore
import com.elink.aigallery.data.db.AppDatabase
import com.elink.aigallery.data.db.FolderWithImages
import com.elink.aigallery.data.db.ImageTag
import com.elink.aigallery.data.db.MediaItem
import com.elink.aigallery.data.db.MediaTagAnalysis
import kotlinx.coroutines.flow.Flow

class MediaRepository(context: Context) {
    private val mediaDao = AppDatabase.getInstance(context).mediaDao()
    private val contentResolver: ContentResolver = context.contentResolver

    fun observeFolders(): Flow<List<FolderWithImages>> = mediaDao.getImagesByFolder()

    suspend fun syncImages() {
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
            null,
            null,
            "${MediaStore.Images.Media.DATE_TAKEN} DESC"
        )?.use { cursor ->
            val pathIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            val dateIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
            val widthIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
            val heightIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)
            val folderIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)

            while (cursor.moveToNext()) {
                val path = cursor.getString(pathIndex) ?: continue
                val dateTaken = cursor.getLong(dateIndex)
                val width = cursor.getInt(widthIndex)
                val height = cursor.getInt(heightIndex)
                val folderName = cursor.getString(folderIndex) ?: "Unknown"

                items.add(
                    MediaItem(
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
            mediaDao.insertMediaItems(items)
        }
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
}
