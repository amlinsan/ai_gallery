package com.elink.aigallery.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ImageEmbeddingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(embedding: ImageEmbedding)

    @Query("SELECT * FROM image_embeddings WHERE mediaId = :mediaId")
    suspend fun getEmbeddingForMedia(mediaId: Long): ImageEmbedding?

    @Query("SELECT * FROM image_embeddings")
    suspend fun getAllEmbeddings(): List<ImageEmbedding>

    @Query("DELETE FROM image_embeddings WHERE mediaId = :mediaId")
    suspend fun deleteEmbedding(mediaId: Long)
    
    @Query("DELETE FROM image_embeddings")
    suspend fun deleteAll()
}
