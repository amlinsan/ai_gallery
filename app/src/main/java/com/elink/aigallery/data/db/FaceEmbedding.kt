package com.elink.aigallery.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "face_embeddings",
    foreignKeys = [
        ForeignKey(
            entity = MediaItem::class,
            parentColumns = ["id"],
            childColumns = ["mediaId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PersonEntity::class,
            parentColumns = ["id"],
            childColumns = ["personId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["mediaId"]), Index(value = ["personId"])]
)
data class FaceEmbedding(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val mediaId: Long,
    val personId: Long?,
    val embedding: ByteArray,
    val embeddingDim: Int,
    val leftPos: Int,
    val topPos: Int,
    val rightPos: Int,
    val bottomPos: Int,
    val createdAt: Long
)
