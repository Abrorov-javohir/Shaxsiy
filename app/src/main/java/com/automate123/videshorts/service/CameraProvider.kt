package com.automate123.videshorts.service

import android.annotation.SuppressLint
import android.content.Context
import androidx.camera.camera2.Camera2Config
import androidx.camera.core.CameraXConfig
import androidx.camera.lifecycle.ProcessCameraProvider
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.Executor
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
@SuppressLint("UnsafeOptInUsageError")
class CameraProvider @Inject constructor(
    private val mainExecutor: Executor
) {

    init {
        ProcessCameraProvider.configureInstance(
            CameraXConfig.Builder
                .fromConfig(Camera2Config.defaultConfig())
                .build()
        )
    }

    suspend fun getInstance(context: Context): ProcessCameraProvider {
        val future = ProcessCameraProvider.getInstance(context)
        return suspendCancellableCoroutine { continuation ->
            future.addListener({
                if (!future.isCancelled) {
                    continuation.resume(future.get())
                } else {
                    continuation.cancel()
                }
            }, mainExecutor)
            continuation.invokeOnCancellation {
                future.cancel(false)
            }
        }
    }
}