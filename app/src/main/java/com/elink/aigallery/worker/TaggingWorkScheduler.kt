package com.elink.aigallery.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.elink.aigallery.utils.MyLog
import java.util.concurrent.TimeUnit

object TaggingWorkScheduler {
    private const val TAG = "TaggingWorkScheduler"
    private const val UNIQUE_STARTUP_WORK = "tagging_startup"
    private const val UNIQUE_CHARGING_WORK = "tagging_charging"

    fun schedule(context: Context) {
        val workManager = WorkManager.getInstance(context.applicationContext)
        MyLog.i(TAG, "Schedule tagging workers")

        // 1. Tagging (Classification)
        val immediateRequest = OneTimeWorkRequestBuilder<TaggingWorker>()
            .addTag(UNIQUE_STARTUP_WORK)
            .build()
        workManager.enqueueUniqueWork(
            UNIQUE_STARTUP_WORK,
            ExistingWorkPolicy.REPLACE,
            immediateRequest
        )

        // 2. Embedding (CLIP) - Scheduled immediately too for testing
        val embeddingRequest = OneTimeWorkRequestBuilder<EmbeddingWorker>()
            .addTag("embedding_startup")
            .build()
        workManager.enqueueUniqueWork(
            "embedding_startup",
            ExistingWorkPolicy.REPLACE,
            embeddingRequest
        )

        // Periodic (Charging)
        val chargingConstraints = Constraints.Builder()
            .setRequiresCharging(true)
            .build()
        
        val chargingRequest = PeriodicWorkRequestBuilder<TaggingWorker>(
            12,
            TimeUnit.HOURS
        )
            .setConstraints(chargingConstraints)
            .addTag(UNIQUE_CHARGING_WORK)
            .build()
        workManager.enqueueUniquePeriodicWork(
            UNIQUE_CHARGING_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            chargingRequest
        )

        val chargingEmbeddingRequest = PeriodicWorkRequestBuilder<EmbeddingWorker>(
            12,
            TimeUnit.HOURS
        )
            .setConstraints(chargingConstraints)
            .addTag("embedding_charging")
            .build()
        workManager.enqueueUniquePeriodicWork(
            "embedding_charging",
            ExistingPeriodicWorkPolicy.KEEP,
            chargingEmbeddingRequest
        )
    }
}
