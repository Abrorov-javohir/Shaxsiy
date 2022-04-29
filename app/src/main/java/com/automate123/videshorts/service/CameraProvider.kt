package com.automate123.videshorts.service

import android.annotation.SuppressLint
import android.content.Context
import androidx.camera.camera2.Camera2Config
import androidx.camera.core.CameraXConfig
import androidx.camera.lifecycle.ProcessCameraProvider
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.Executor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@SuppressLint("UnsafeOptInUsageError")
class CameraProvider @Inject constructor(
    @ApplicationContext context: Context,
    mainExecutor: Executor
) {

    val instance = MutableStateFlow<ProcessCameraProvider?>(null)

    private var future: ListenableFuture<ProcessCameraProvider>? = null

    init {
        ProcessCameraProvider.configureInstance(
            CameraXConfig.Builder.fromConfig(Camera2Config.defaultConfig())
                .build()
        )
        GlobalScope.launch {
            PermProvider.allGranted.collect {
                if (it) {
                    future = ProcessCameraProvider.getInstance(context)
                    future?.addListener({
                        val cameraProvider = future?.get()
                        instance.tryEmit(cameraProvider)
                    }, mainExecutor)
                } else {
                    future?.cancel(true)
                    instance.emit(null)
                }
            }
        }
    }
}