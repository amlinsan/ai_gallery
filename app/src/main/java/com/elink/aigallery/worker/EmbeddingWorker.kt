package com.elink.aigallery.worker

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.elink.aigallery.ai.ClipHelper
import com.elink.aigallery.data.repository.MediaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EmbeddingWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val repository = MediaRepository(context)

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.i(TAG, "Starting EmbeddingWorker...")
            val clipHelper = ClipHelper.getInstance(applicationContext)

            // Batch process
            while (true) {
                if (isStopped) break
                
                // Fetch images that need embedding
                val mediaItems = repository.getImagesWithoutEmbedding(BATCH_SIZE)
                if (mediaItems.isEmpty()) {
                    break
                }

                for (item in mediaItems) {
                    if (isStopped) break
                    
                    try {
                        // Load bitmap
                        val bitmap = BitmapFactory.decodeFile(item.path)
                        if (bitmap != null) {
                            val embedding = clipHelper.embedImage(bitmap)
                            if (embedding != null) {
                                repository.insertEmbedding(item.id, embedding)
                                Log.d(TAG, "Embedded media: ${item.id}")
                            } else {
                                Log.e(TAG, "Failed to embed media: ${item.id} (model error)")
                            }
                            bitmap.recycle()
                        } else {
                            Log.w(TAG, "Failed to decode file: ${item.path}")
                            // Insert dummy or skip to avoid infinite loop? 
                            // For now, we might get stuck if file is corrupt. 
                            // In prod, mark as 'processed_failed' or similar.
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing media ${item.id}", e)
                    }
                }
            }

            Log.i(TAG, "EmbeddingWorker finished.")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "EmbeddingWorker failed", e)
            Result.failure()
        }
    }

    companion object {
        private const val TAG = "EmbeddingWorker"
        private const val BATCH_SIZE = 10
    }
}
