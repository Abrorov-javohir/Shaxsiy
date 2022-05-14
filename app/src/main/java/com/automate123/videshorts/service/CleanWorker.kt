package com.automate123.videshorts.service

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.lifecycle.LiveData
import androidx.work.*
import com.automate123.videshorts.KEY_DIRNAME
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File

@HiltWorker
class CleanWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted parameters: WorkerParameters,
    private val rootDir: File
) : CoroutineWorker(context, parameters) {

    override suspend fun doWork(): Result {
        with(applicationContext) {
            val dirname = inputData.getString(KEY_DIRNAME)!!
            try {
                withContext(Dispatchers.IO) {
                    val dirs = rootDir.listFiles()
                        ?.sorted()
                        ?.takeWhile { it.name != dirname }
                        .orEmpty()
                    dirs.forEach {
                        it.deleteRecursively()
                    }
                }
                return Result.success()
            } catch (e: Throwable) {
                Timber.e(e)
            }
        }
        return Result.failure()
    }

    companion object {

        private const val NAME = "clean"

        fun launch(context: Context, dirname: String): LiveData<WorkInfo> {
            val request = OneTimeWorkRequestBuilder<CleanWorker>()
                .setInputData(Data.Builder()
                    .putString(KEY_DIRNAME, dirname)
                    .build())
                .build()
            with(WorkManager.getInstance(context)) {
                enqueueUniqueWork(NAME, ExistingWorkPolicy.REPLACE, request)
                return getWorkInfoByIdLiveData(request.id)
            }
        }

        fun cancel(context: Context) {
            with(WorkManager.getInstance(context)) {
                cancelUniqueWork(NAME)
            }
        }
    }
}