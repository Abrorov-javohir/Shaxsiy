package com.automate123.videshorts.service

import android.annotation.SuppressLint
import android.content.Context
import androidx.work.*
import androidx.work.impl.utils.futures.SettableFuture
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.*

@SuppressLint("RestrictedApi")
abstract class MyCoroutineWorker(
    appContext: Context,
    params: WorkerParameters
) : ListenableWorker(appContext, params) {

    protected val job = Job()
    protected val future: SettableFuture<Result> = SettableFuture.create()

    init {
        future.addListener({
            if (future.isCancelled) {
                job.cancel()
            }
        }, taskExecutor.backgroundExecutor)
    }

    @Deprecated(message = "use withContext(...) inside doWork() instead.")
    open val coroutineContext: CoroutineDispatcher = Dispatchers.Default

    @Suppress("DEPRECATION")
    override fun startWork(): ListenableFuture<Result> {
        val coroutineScope = CoroutineScope(coroutineContext + job)
        coroutineScope.launch {
            try {
                val result = doWork()
                future.set(result)
            } catch (e: Throwable) {
                future.setException(e)
            }
        }
        return future
    }

    abstract suspend fun doWork(): Result

    suspend fun setProgress(data: Data) {
        setProgressAsync(data).await()
    }

    suspend fun setForeground(foregroundInfo: ForegroundInfo) {
        setForegroundAsync(foregroundInfo).await()
    }

    override fun onStopped() {
        job.cancel()
        //future.cancel(true)
    }
}
