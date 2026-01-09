package com.elink.aigallery.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.elink.aigallery.data.model.PersonAlbum
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonDao {
    @Query("SELECT * FROM persons")
    suspend fun getPersons(): List<PersonEntity>

    @Query(
        """
        SELECT p.id AS personId,
               p.name AS name,
               COUNT(DISTINCT f.mediaId) AS mediaCount,
               (
                   SELECT m.path FROM media_items AS m
                   INNER JOIN face_embeddings AS f2 ON f2.mediaId = m.id
                   WHERE f2.personId = p.id
                   ORDER BY m.dateTaken DESC
                   LIMIT 1
               ) AS coverPath,
               (
                   SELECT MAX(m.dateTaken) FROM media_items AS m
                   INNER JOIN face_embeddings AS f3 ON f3.mediaId = m.id
                   WHERE f3.personId = p.id
               ) AS latestDate
        FROM persons AS p
        INNER JOIN face_embeddings AS f ON f.personId = p.id
        GROUP BY p.id
        ORDER BY latestDate DESC
        """
    )
    fun observePersonAlbums(): Flow<List<PersonAlbum>>

    @Query(
        """
        SELECT DISTINCT m.* FROM media_items AS m
        INNER JOIN face_embeddings AS f ON f.mediaId = m.id
        WHERE f.personId = :personId
        ORDER BY m.dateTaken DESC
        """
    )
    suspend fun getMediaByPerson(personId: Long): List<MediaItem>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPerson(person: PersonEntity): Long

    @Update
    suspend fun updatePerson(person: PersonEntity)

    @Query("UPDATE persons SET name = :newName WHERE id = :personId")
    suspend fun updateName(personId: Long, newName: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFaceEmbeddings(embeddings: List<FaceEmbedding>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMediaFaceAnalysis(analysis: MediaFaceAnalysis)
}
