package com.elink.aigallery.data.model

data class PersonAlbum(
    val personId: Long,
    val name: String,
    val coverPath: String?,
    val mediaCount: Int,
    val latestDate: Long
)
