package com.elink.aigallery.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMediaItems(items: List<MediaItem>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImageTags(tags: List<ImageTag>)

    @Query(
        """
        SELECT DISTINCT m.* FROM media_items AS m
        LEFT JOIN image_tags AS t ON m.id = t.mediaId
        WHERE t.label LIKE '%' || :query || '%' 
           OR t.label LIKE '%' || :mappedQuery || '%'
           OR m.path LIKE '%' || :query || '%'
           OR m.folderName LIKE '%' || :query || '%'
        ORDER BY m.dateTaken DESC
        """
    )
    fun searchImages(query: String, mappedQuery: String): Flow<List<MediaItem>>

    @Transaction
    @Query(
        """
        SELECT DISTINCT folderName FROM media_items
        ORDER BY folderName ASC
        """
    )
    fun getImagesByFolder(): Flow<List<FolderWithImages>>

    @Query(
        """
        SELECT m.* FROM media_items AS m
        LEFT JOIN media_tag_analysis AS a ON m.id = a.mediaId
        WHERE a.mediaId IS NULL
        ORDER BY m.dateTaken DESC
        LIMIT :limit
        """
    )
    suspend fun getImagesWithoutTagAnalysis(limit: Int): List<MediaItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMediaTagAnalysis(analysis: MediaTagAnalysis)

    @Query(
        """
        SELECT m.* FROM media_items AS m
        LEFT JOIN media_face_analysis AS f ON m.id = f.mediaId
        WHERE f.mediaId IS NULL
        ORDER BY m.dateTaken DESC
        LIMIT :limit
        """
    )
    suspend fun getImagesWithoutFaceAnalysis(limit: Int): List<MediaItem>

    @Query(
        """
        SELECT DISTINCT m.* FROM media_items AS m
        INNER JOIN image_tags AS t ON m.id = t.mediaId
        WHERE t.label = :label
        ORDER BY m.dateTaken DESC
        """
    )
    fun getImagesByLabel(label: String): Flow<List<MediaItem>>

    @Query("DELETE FROM media_items WHERE path = :path")
    suspend fun deleteMediaItemByPath(path: String)

    @Query("DELETE FROM media_items WHERE id IN (:ids)")
    suspend fun deleteMediaItems(ids: List<Long>)

    @Query("SELECT * FROM media_items WHERE path = :path LIMIT 1")
    suspend fun getMediaItemByPath(path: String): MediaItem?

    @Query("UPDATE media_items SET mediaStoreId = :mediaStoreId, dateTaken = :dateTaken, width = :width, height = :height WHERE path = :path")
    suspend fun updateMediaItemInfo(path: String, mediaStoreId: Long, dateTaken: Long, width: Int, height: Int): Int
}
