package com.automate123.videshorts.data

import android.content.Context
import androidx.camera.video.FileOutputOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

class FileManager @Inject constructor(
    @ApplicationContext context: Context
) {

    private val internalDir = context.filesDir

    fun getOutputOptions(position: Int): FileOutputOptions {
        return FileOutputOptions
            .Builder(File(internalDir, "$position.mp4"))
            .build()
    }
}