package com.elink.aigallery.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "image_embeddings",
    foreignKeys = [
        ForeignKey(
            entity = MediaItem::class,
            parentColumns = ["id"],
            childColumns = ["mediaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["mediaId"])]
)
data class ImageEmbedding(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val mediaId: Long,
    val embedding: ByteArray, // Store float array as bytes
    val embeddingDim: Int,
    val modelVersion: String = "clip_v1",
    val createdAt: Long = System.currentTimeMillis()
)
