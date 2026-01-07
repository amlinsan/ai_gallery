package com.elink.aigallery.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "media_face_analysis",
    primaryKeys = ["mediaId"],
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
data class MediaFaceAnalysis(
    val mediaId: Long,
    val hasFace: Boolean,
    val processedAt: Long
)
