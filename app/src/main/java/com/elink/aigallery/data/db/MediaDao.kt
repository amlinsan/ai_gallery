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
        INNER JOIN image_tags AS t ON m.id = t.mediaId
        WHERE t.label LIKE '%' || :query || '%'
        ORDER BY m.dateTaken DESC
        """
    )
    fun searchImages(query: String): Flow<List<MediaItem>>

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
        LEFT JOIN image_tags AS t ON m.id = t.mediaId
        WHERE t.mediaId IS NULL
        ORDER BY m.dateTaken DESC
        LIMIT :limit
        """
    )
    suspend fun getUntaggedImages(limit: Int): List<MediaItem>

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
}
