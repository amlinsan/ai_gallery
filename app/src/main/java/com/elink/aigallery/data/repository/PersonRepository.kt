package com.elink.aigallery.data.repository

import android.content.Context
import com.elink.aigallery.data.db.AppDatabase
import com.elink.aigallery.data.db.FaceEmbedding
import com.elink.aigallery.data.db.MediaFaceAnalysis
import com.elink.aigallery.data.db.MediaItem
import com.elink.aigallery.data.db.PersonEntity
import com.elink.aigallery.data.model.PersonAlbum
import kotlinx.coroutines.flow.Flow

class PersonRepository(context: Context) {
    private val mediaDao = AppDatabase.getInstance(context).mediaDao()
    private val personDao = AppDatabase.getInstance(context).personDao()

    suspend fun getPersons(): List<PersonEntity> = personDao.getPersons()

    fun observePersonAlbums(): Flow<List<PersonAlbum>> = personDao.observePersonAlbums()

    suspend fun getMediaByPerson(personId: Long): List<MediaItem> {
        return personDao.getMediaByPerson(personId)
    }

    suspend fun insertPerson(person: PersonEntity): Long = personDao.insertPerson(person)

    suspend fun updatePerson(person: PersonEntity) = personDao.updatePerson(person)

    suspend fun updatePersonName(id: Long, name: String) = personDao.updateName(id, name)

    suspend fun insertFaceEmbeddings(embeddings: List<FaceEmbedding>) {
        if (embeddings.isNotEmpty()) {
            personDao.insertFaceEmbeddings(embeddings)
        }
    }

    suspend fun upsertMediaFaceAnalysis(analysis: MediaFaceAnalysis) {
        personDao.upsertMediaFaceAnalysis(analysis)
    }

    suspend fun getImagesWithoutFaceAnalysis(limit: Int): List<MediaItem> {
        return mediaDao.getImagesWithoutFaceAnalysis(limit)
    }
}
