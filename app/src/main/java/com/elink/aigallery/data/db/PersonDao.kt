package com.elink.aigallery.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface PersonDao {
    @Query("SELECT * FROM persons")
    suspend fun getPersons(): List<PersonEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPerson(person: PersonEntity): Long

    @Update
    suspend fun updatePerson(person: PersonEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFaceEmbeddings(embeddings: List<FaceEmbedding>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMediaFaceAnalysis(analysis: MediaFaceAnalysis)
}
