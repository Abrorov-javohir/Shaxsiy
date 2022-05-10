package com.automate123.videshorts.service

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.lifecycle.LiveData
import androidx.work.*
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import com.arthenica.ffmpegkit.Session
import com.automate123.videshorts.KEY_DIRNAME
import com.automate123.videshorts.KEY_FILENAME
import com.automate123.videshorts.extension.qPath
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.security.MessageDigest

@HiltWorker
class VideoWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted parameters: WorkerParameters,
    private val rootDir: File
) : CoroutineWorker(context, parameters) {

    override suspend fun doWork(): Result {
        with(applicationContext) {
            val dirname = inputData.getString(KEY_DIRNAME)!!
            val sessions = mutableListOf<Session>()
            try {
                val workDir = File(rootDir, dirname)
                val videoFiles = mutableListOf<File>()
                val outputFile: File

                withContext(Dispatchers.IO) {
                    videoFiles.addAll(workDir.listFiles()
                        ?.filter { it.name.matches("^[0-9]+\\.mp4$".toRegex()) }
                        ?.sorted()
                        .orEmpty())
                    check(videoFiles.isNotEmpty())

                    val md5 = MessageDigest.getInstance("MD5")
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    videoFiles.forEach { file ->
                        file.inputStream().use {
                            var bytes = it.read(buffer)
                            while (bytes >= 0) {
                                md5.update(buffer, 0, bytes)
                                bytes = it.read(buffer)
                            }
                        }
                    }
                    val hash = md5.digest().joinToString("") { "%02x".format(it) }
                    outputFile = File(workDir, "$hash.mp4")
                }

                if (outputFile.exists()) {
                    return Result.success(workDataOf(KEY_FILENAME to outputFile.name))
                }

                if (videoFiles.size < 2) {
                    withContext(Dispatchers.IO) {
                        videoFiles.first().copyTo(outputFile, true)
                    }
                    return Result.success(workDataOf(KEY_FILENAME to outputFile.name))
                }

                withContext(Dispatchers.IO) {
                    val listFile = File(workDir, "$id.txt")
                    try {
                        listFile.writeText(videoFiles.joinToString("\n") { "file ${it.qPath}" })

                        sessions.add(FFmpegKit.execute("""
                            -y -f concat -safe 0 -i ${listFile.qPath} -c copy ${outputFile.qPath}
                        """.trim()))
                        if (!ReturnCode.isSuccess(sessions.last().returnCode)) {
                            throw Throwable(sessions.last().failStackTrace)
                        }
                    } finally {
                        listFile.delete()
                    }
                }
                return Result.success(workDataOf(KEY_FILENAME to outputFile.name))
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

        fun launch(context: Context, dirname: String): LiveData<WorkInfo> {
            val request = OneTimeWorkRequestBuilder<VideoWorker>()
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