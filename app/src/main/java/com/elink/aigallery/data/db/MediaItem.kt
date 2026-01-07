package com.elink.aigallery.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "media_items",
    indices = [Index(value = ["path"], unique = true), Index(value = ["folderName"])]
)
data class MediaItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val path: String,
    val dateTaken: Long,
    val folderName: String,
    val width: Int,
    val height: Int
)
