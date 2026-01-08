package com.elink.aigallery.data.model

import com.elink.aigallery.data.db.MediaItem

data class CategoryAlbum(
    val title: String,
    val type: CategoryType,
    val items: List<MediaItem>
)

enum class CategoryType {
    PEOPLE,
    FOOD,
    NATURE
}
