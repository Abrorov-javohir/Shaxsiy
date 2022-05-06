package com.automate123.videshorts

import android.app.Application
import androidx.camera.camera2.Camera2Config
import androidx.camera.core.CameraXConfig
import com.arthenica.ffmpegkit.FFmpegKit
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
@Suppress("unused")
open class MainApp : Application(), CameraXConfig.Provider {

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        FFmpegKit.cancel(0)
    }

    override fun getCameraXConfig(): CameraXConfig {
        return CameraXConfig.Builder
            .fromConfig(Camera2Config.defaultConfig())
            .build();
    }
}