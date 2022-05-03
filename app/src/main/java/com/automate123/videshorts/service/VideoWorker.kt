package com.automate123.videshorts.service

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.work.*
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import com.arthenica.ffmpegkit.Session
import com.automate123.videshorts.EXTRA_FILENAME
import com.automate123.videshorts.extension.qPath
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.time.Instant
import java.time.format.DateTimeFormatter

class VideoWorker(context: Context, parameters: WorkerParameters) : CoroutineWorker(context, parameters) {

    override suspend fun doWork(): Result {
        with(applicationContext) {
            val dirname = inputData.getString(EXTRA_DIRNAME)!!
            val sessions = mutableListOf<Session>()
            try {
                val dir = File(cacheDir, dirname)
                check(dir.exists())

                val videoFiles = mutableListOf<File>()
                withContext(Dispatchers.IO) {
                    videoFiles.addAll(dir.listFiles()
                        .filter { it.name.matches("^[0-9]+\\.mp4$".toRegex()) }
                        .sorted())
                    check(videoFiles.isNotEmpty())
                }

                val outputFile = File(dir, nameFormatter.format(Instant.now()))
                val outputData = Data.Builder()
                    .putString(EXTRA_FILENAME, outputFile.name)
                    .build()

                if (videoFiles.size < 2) {
                    withContext(Dispatchers.IO) {
                        videoFiles.first().copyTo(outputFile)
                    }
                    return Result.success(outputData)
                }

                withContext(Dispatchers.IO) {
                    val listFile = File(dir, "$id.txt")
                    listFile.writeText(videoFiles.joinToString("\n") { "file ${it.qPath}" })
                    sessions.add(FFmpegKit.execute("-y -f concat -safe 0 -i ${listFile.qPath} -c copy ${outputFile.qPath}"))
                    if (!ReturnCode.isSuccess(sessions.last().returnCode)) {
                        throw Throwable(sessions.last().failStackTrace)
                    }
                }
                return Result.success(outputData)
            } catch (e: CancellationException) {
                sessions.forEach {
                    FFmpegKit.cancel(it.sessionId)
                }
            } catch (e: Throwable) {
                Timber.e(e)
            }
        }
        return Result.failure()
    }

    companion object {

        const val NAME = "video"

        private const val EXTRA_DIRNAME = "dirname"

        private val nameFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss'.mp4'")

        fun launch(context: Context, dirname: String): LiveData<WorkInfo> {
            val request = OneTimeWorkRequestBuilder<VideoWorker>()
                .setInputData(Data.Builder()
                    .putString(EXTRA_DIRNAME, dirname)
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