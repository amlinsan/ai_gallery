package com.elink.aigallery.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "image_tags",
    primaryKeys = ["mediaId", "label"],
    foreignKeys = [
        ForeignKey(
            entity = MediaItem::class,
            parentColumns = ["id"],
            childColumns = ["mediaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["mediaId"]), Index(value = ["label"])]
)
data class ImageTag(
    val mediaId: Long,
    val label: String,
    val confidence: Float
)
