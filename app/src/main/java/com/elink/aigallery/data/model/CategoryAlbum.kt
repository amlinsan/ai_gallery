package com.elink.aigallery.data.model

import com.elink.aigallery.data.db.MediaItem

data class CategoryAlbum(
    val title: String,
    val items: List<MediaItem>
)
