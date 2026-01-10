package com.elink.aigallery.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import com.elink.aigallery.data.db.MediaTagAnalysis

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
        LEFT JOIN face_embeddings AS fe ON m.id = fe.mediaId
        LEFT JOIN persons AS p ON fe.personId = p.id
        WHERE t.label LIKE '%' || :query || '%' 
           OR t.label LIKE '%' || :mappedQuery || '%'
           OR m.path LIKE '%' || :query || '%'
           OR m.folderName LIKE '%' || :query || '%'
           OR p.name LIKE '%' || :query || '%'
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

    @Query("""
        SELECT * FROM media_items 
        WHERE id NOT IN (SELECT mediaId FROM media_tag_analysis)
        LIMIT :limit
    """)
    suspend fun getImagesWithoutTagAnalysis(limit: Int): List<MediaItem>
    
    @Query("""
        SELECT * FROM media_items 
        WHERE id NOT IN (SELECT mediaId FROM media_face_analysis)
        LIMIT :limit
    """)
    suspend fun getImagesWithoutFaceAnalysis(limit: Int): List<MediaItem>

    @Query("""
        SELECT * FROM media_items 
        WHERE id NOT IN (SELECT mediaId FROM image_embeddings)
        LIMIT :limit
    """)
    suspend fun getImagesWithoutEmbedding(limit: Int): List<MediaItem>

    @Query("""
        SELECT * FROM media_items 
        WHERE id IN (:ids)
    """)
    suspend fun getMediaItemsByIds(ids: List<Long>): List<MediaItem>

    @Query("UPDATE media_items SET path = :path, dateTaken = :dateTaken, width = :width, height = :height WHERE mediaStoreId = :mediaStoreId")
    suspend fun updateMediaItemInfo(path: String, mediaStoreId: Long, dateTaken: Long, width: Int, height: Int)

    @Query("DELETE FROM media_items WHERE path = :path")
    suspend fun deleteMediaItemByPath(path: String)

    @Query("DELETE FROM media_items WHERE id IN (:ids)")
    suspend fun deleteMediaItems(ids: List<Long>)

    @Query("SELECT * FROM media_items WHERE path = :path")
    suspend fun getMediaItemByPath(path: String): MediaItem?

    @Query("""
        SELECT DISTINCT m.* FROM media_items m
        INNER JOIN image_tags t ON m.id = t.mediaId
        WHERE t.label = :label
        ORDER BY m.dateTaken DESC
    """)
    fun getImagesByLabel(label: String): Flow<List<MediaItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMediaTagAnalysis(analysis: MediaTagAnalysis)
}
