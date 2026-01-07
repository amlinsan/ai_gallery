package com.elink.aigallery.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "persons",
    indices = [Index(value = ["name"])]
)
data class PersonEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val embedding: ByteArray,
    val embeddingDim: Int,
    val sampleCount: Int,
    val createdAt: Long
)
