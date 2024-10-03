package com.epicmillennium.cheshmap.utils

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import com.epicmillennium.cheshmap.utils.workers.WaterSourcesRetrieverWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class WaterSourcesRetrieverWorkerUtils @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        const val WATER_SOURCES_RETRIEVER_WORKER = "WATER_SOURCES_RETRIEVER_WORKER"
        const val WATER_SOURCES_RETRIEVER_WORKER_TAG = "WATER_SOURCES_RETRIEVER_WORKER_TAG"
    }

    fun retrieveWaterSources() = triggerWaterSourceRetrievingWorker()

    private fun triggerWaterSourceRetrievingWorker() {
        val workRequest = OneTimeWorkRequestBuilder<WaterSourcesRetrieverWorker>()
            .addTag(WATER_SOURCES_RETRIEVER_WORKER)
            .setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.SECONDS)
            .setExpedited(OutOfQuotaPolicy.DROP_WORK_REQUEST)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            WATER_SOURCES_RETRIEVER_WORKER_TAG,
            ExistingWorkPolicy.KEEP,
            workRequest
        )
    }
}