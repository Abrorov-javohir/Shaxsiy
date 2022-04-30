package com.automate123.videshorts.data

import android.content.ContentValues
import android.content.Context
import android.provider.MediaStore
import androidx.camera.video.MediaStoreOutputOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class FileManager @Inject constructor(
    @ApplicationContext context: Context
) {

    private val contentResolver = context.contentResolver

    fun getMediaOptions(index: Int): MediaStoreOutputOptions {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, index.toString())
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
        }
        return MediaStoreOutputOptions
            .Builder(contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            .setContentValues(contentValues)
            .build()
    }
}