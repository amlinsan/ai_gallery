package com.elink.aigallery.worker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.os.Build
import com.elink.aigallery.utils.MyLog
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.elink.aigallery.ImageClassifierHelper
import com.elink.aigallery.ai.EmbeddingUtils
import com.elink.aigallery.ai.FaceEmbeddingHelper
import com.elink.aigallery.data.db.FaceEmbedding
import com.elink.aigallery.data.db.MediaFaceAnalysis
import com.elink.aigallery.data.db.PersonEntity
import com.elink.aigallery.data.db.MediaTagAnalysis
import com.elink.aigallery.data.repository.MediaRepository
import com.elink.aigallery.data.repository.PersonRepository
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.resume

class TaggingWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val hasPermission = hasReadPermission(applicationContext)
        MyLog.i(TAG, "Worker start, hasPermission=$hasPermission")
        if (!hasPermission) {
            MyLog.w(TAG, "Media permission not granted. Skip tagging.")
            return@withContext Result.success()
        }

        val mediaRepository = MediaRepository(applicationContext)
        val personRepository = PersonRepository(applicationContext)
        val classifier = ImageClassifierHelper.getSharedInstance(applicationContext)
        val faceDetector = FaceDetection.getClient(faceOptions())
        val faceEmbeddingHelper = try {
            FaceEmbeddingHelper.getInstance(applicationContext)
        } catch (e: Exception) {
            MyLog.e(TAG, "Face embedding model is not available", e)
            null
        }

        try {
            MyLog.i(TAG, "Tagging worker started")
            processTags(mediaRepository, classifier)
            if (faceEmbeddingHelper != null) {
                processFaces(mediaRepository, personRepository, faceDetector, faceEmbeddingHelper)
            }
            MyLog.i(TAG, "Tagging worker finished")
            Result.success()
        } catch (e: Exception) {
            MyLog.e(TAG, "Tagging worker failed", e)
            Result.retry()
        } finally {
            faceDetector.close()
        }
    }

    private suspend fun processTags(
        mediaRepository: MediaRepository,
        classifier: ImageClassifierHelper
    ) {
        var batch = mediaRepository.getImagesWithoutTagAnalysis(BATCH_LIMIT)
        if (batch.isEmpty()) {
            MyLog.i(TAG, "No images pending label analysis")
        }
        while (batch.isNotEmpty() && !isStopped) {
            MyLog.i(TAG, "Tagging batch size=${batch.size}")
            for (item in batch) {
                if (isStopped) break
                val bitmap = decodeScaledBitmap(
                    item.path,
                    MAX_EDGE_CLASSIFY,
                    Bitmap.Config.ARGB_8888
                ) ?: continue
                val now = System.currentTimeMillis()
                try {
                    val labels = classifier.classifyLabels(bitmap)
                    mediaRepository.insertTags(item.id, labels)
                    mediaRepository.upsertMediaTagAnalysis(
                        MediaTagAnalysis(
                            mediaId = item.id,
                            labelCount = labels.size,
                            processedAt = now
                        )
                    )
                    MyLog.i(TAG, "Tags for ${item.id}: count=${labels.size}")
                } catch (e: Exception) {
                    MyLog.e(TAG, "Image tagging failed: ${item.path}", e)
                    mediaRepository.upsertMediaTagAnalysis(
                        MediaTagAnalysis(
                            mediaId = item.id,
                            labelCount = 0,
                            processedAt = now
                        )
                    )
                } finally {
                    bitmap.recycle()
                }
            }
            batch = mediaRepository.getImagesWithoutTagAnalysis(BATCH_LIMIT)
        }
    }

    private suspend fun processFaces(
        mediaRepository: MediaRepository,
        personRepository: PersonRepository,
        faceDetector: FaceDetector,
        faceEmbeddingHelper: FaceEmbeddingHelper
    ) {
        val people = personRepository.getPersons()
            .map { PersonCandidate(it, EmbeddingUtils.toFloatArray(it.embedding)) }
            .toMutableList()

        var batch = personRepository.getImagesWithoutFaceAnalysis(BATCH_LIMIT)
        if (batch.isEmpty()) {
            MyLog.i(TAG, "No images pending face analysis")
        }
        while (batch.isNotEmpty() && !isStopped) {
            MyLog.i(TAG, "Face analysis batch size=${batch.size}")
            for (item in batch) {
                if (isStopped) break
                val bitmap = decodeScaledBitmap(
                    item.path,
                    MAX_EDGE_FACE,
                    Bitmap.Config.ARGB_8888
                ) ?: continue
                val faces = detectFaces(faceDetector, bitmap)
                val now = System.currentTimeMillis()
                val embeddings = mutableListOf<FaceEmbedding>()
                if (faces.isNotEmpty()) {
                    mediaRepository.insertTags(item.id, listOf(PERSON_TAG))
                    for (face in faces) {
                        val cropped = cropFace(bitmap, face.boundingBox) ?: continue
                        val vector = try {
                            faceEmbeddingHelper.embed(cropped)
                        } catch (e: Exception) {
                            MyLog.e(TAG, "Face embedding failed for ${item.id}", e)
                            null
                        }
                        cropped.recycle()
                        if (vector == null) continue

                        val matched = matchPerson(vector, people, personRepository, now)
                        val rect = face.boundingBox
                        embeddings.add(
                            FaceEmbedding(
                                mediaId = item.id,
                                personId = matched.id,
                                embedding = EmbeddingUtils.toByteArray(vector),
                                embeddingDim = vector.size,
                                leftPos = rect.left,
                                topPos = rect.top,
                                rightPos = rect.right,
                                bottomPos = rect.bottom,
                                createdAt = now
                            )
                        )
                    }
                }
                personRepository.insertFaceEmbeddings(embeddings)
                personRepository.upsertMediaFaceAnalysis(
                    MediaFaceAnalysis(
                        mediaId = item.id,
                        hasFace = faces.isNotEmpty(),
                        processedAt = now
                    )
                )
                bitmap.recycle()
            }
            batch = personRepository.getImagesWithoutFaceAnalysis(BATCH_LIMIT)
        }
    }

    private suspend fun detectFaces(
        faceDetector: FaceDetector,
        bitmap: Bitmap
    ): List<Face> {
        return suspendCancellableCoroutine { cont ->
            val image = InputImage.fromBitmap(bitmap, 0)
            faceDetector.process(image)
                .addOnSuccessListener { faces ->
                    if (cont.isActive) cont.resume(faces)
                }
                .addOnFailureListener {
                    if (cont.isActive) cont.resume(emptyList())
                }
        }
    }

    private suspend fun matchPerson(
        embedding: FloatArray,
        people: MutableList<PersonCandidate>,
        personRepository: PersonRepository,
        now: Long
    ): PersonEntity {
        var best: PersonCandidate? = null
        var bestScore = -1f
        for (candidate in people) {
            val score = EmbeddingUtils.cosineSimilarity(embedding, candidate.embedding)
            if (score > bestScore) {
                bestScore = score
                best = candidate
            }
        }
        return if (best != null && bestScore >= PERSON_MATCH_THRESHOLD) {
            val updatedEmbedding = EmbeddingUtils.mergeAverage(
                best.embedding,
                best.person.sampleCount,
                embedding
            )
            val updatedPerson = best.person.copy(
                embedding = EmbeddingUtils.toByteArray(updatedEmbedding),
                sampleCount = best.person.sampleCount + 1
            )
            personRepository.updatePerson(updatedPerson)
            best.person = updatedPerson
            best.embedding = updatedEmbedding
            updatedPerson
        } else {
            val newPerson = PersonEntity(
                name = DEFAULT_PERSON_NAME,
                embedding = EmbeddingUtils.toByteArray(embedding),
                embeddingDim = embedding.size,
                sampleCount = 1,
                createdAt = now
            )
            val newId = personRepository.insertPerson(newPerson)
            val created = newPerson.copy(id = newId)
            people.add(PersonCandidate(created, embedding))
            created
        }
    }

    private fun cropFace(bitmap: Bitmap, rect: Rect): Bitmap? {
        val marginX = (rect.width() * FACE_MARGIN_RATIO).toInt()
        val marginY = (rect.height() * FACE_MARGIN_RATIO).toInt()
        val left = (rect.left - marginX).coerceAtLeast(0)
        val top = (rect.top - marginY).coerceAtLeast(0)
        val right = (rect.right + marginX).coerceAtMost(bitmap.width)
        val bottom = (rect.bottom + marginY).coerceAtMost(bitmap.height)
        val width = right - left
        val height = bottom - top
        if (width <= 0 || height <= 0) return null
        return Bitmap.createBitmap(bitmap, left, top, width, height)
    }

    private fun decodeScaledBitmap(
        path: String,
        maxEdge: Int,
        config: Bitmap.Config
    ): Bitmap? {
        return try {
            val file = File(path)
            if (!file.exists()) return null
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(path, bounds)
            val sampleSize = calculateInSampleSize(bounds, maxEdge)
            val options = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = config
            }
            BitmapFactory.decodeFile(path, options)
        } catch (e: Exception) {
            MyLog.e(TAG, "Decode bitmap failed: $path", e)
            null
        }
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        maxEdge: Int
    ): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > maxEdge || width > maxEdge) {
            var halfHeight = height / 2
            var halfWidth = width / 2
            while (halfHeight / inSampleSize >= maxEdge && halfWidth / inSampleSize >= maxEdge) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun faceOptions(): FaceDetectorOptions {
        return FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
            .enableTracking()
            .build()
    }

    private fun hasReadPermission(context: Context): Boolean {
        return when {
            Build.VERSION.SDK_INT >= 34 -> {
                val hasImages = isGranted(context, Manifest.permission.READ_MEDIA_IMAGES)
                val hasVideo = isGranted(context, Manifest.permission.READ_MEDIA_VIDEO)
                val hasSelected =
                    isGranted(context, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
                hasImages || hasVideo || hasSelected
            }
            Build.VERSION.SDK_INT >= 33 -> {
                val hasImages = isGranted(context, Manifest.permission.READ_MEDIA_IMAGES)
                val hasVideo = isGranted(context, Manifest.permission.READ_MEDIA_VIDEO)
                hasImages || hasVideo
            }
            else -> isGranted(context, Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    private fun isGranted(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private data class PersonCandidate(
        var person: PersonEntity,
        var embedding: FloatArray
    )

    companion object {
        private const val TAG = "TaggingWorker"
        private const val BATCH_LIMIT = 50
        private const val MAX_EDGE_CLASSIFY = 1024
        private const val MAX_EDGE_FACE = 640
        private const val FACE_MARGIN_RATIO = 0.2f
        private const val PERSON_MATCH_THRESHOLD = 0.6f
        private const val DEFAULT_PERSON_NAME = "Unknown"
        private const val PERSON_TAG = "Person"
    }
}
