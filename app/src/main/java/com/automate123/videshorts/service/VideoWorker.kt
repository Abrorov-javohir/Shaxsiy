package com.automate123.videshorts.service

import android.content.Context
import androidx.work.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class VideoWorker(context: Context, parameters: WorkerParameters) : CoroutineWorker(context, parameters) {

    override suspend fun doWork(): Result {
        with(applicationContext) {
            withContext(Dispatchers.IO) {

            }
            return Result.success()
        }
    }

    companion object {

        fun launch(context: Context) {
            val request = OneTimeWorkRequestBuilder<VideoWorker>()
                .build()
            with(WorkManager.getInstance(context)) {
                enqueueUniqueWork(VideoWorker::class.java.name, ExistingWorkPolicy.REPLACE, request)
            }
        }
    }
}