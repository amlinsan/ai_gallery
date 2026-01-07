package com.elink.aigallery.data.db

import androidx.room.ColumnInfo
import androidx.room.Relation

data class FolderWithImages(
    @ColumnInfo(name = "folderName") val folderName: String,
    @Relation(
        parentColumn = "folderName",
        entityColumn = "folderName"
    )
    val items: List<MediaItem>
)
