package com.automate123.videshorts.service

import android.content.Context
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.VideoRecordEvent
import androidx.camera.video.VideoRecordEvent.Finalize.*
import com.automate123.videshorts.data.Preferences
import com.automate123.videshorts.extension.currentTimeInSeconds
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import kotlin.math.max

@ViewModelScoped
class ShortsController @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferences: Preferences,
    private val rootDir: File
) : CoroutineScope {

    @Volatile
    var isCameraBound = false

    private val _isRecording = MutableStateFlow(false)
    val isRecording = _isRecording.asStateFlow()

    private var file: File? = null
        set(value) {
            field = value
            _recordFile.tryEmit(value)
        }
    private val _recordFile = MutableSharedFlow<File?>(0, 1, DROP_OLDEST)
    val recordFile = _recordFile.asSharedFlow()

    private var position = 0
        set(value) {
            field = value
            _recordPosition.value = value
        }
    private val _recordPosition = MutableStateFlow(position)
    val recordPosition = _recordPosition.asStateFlow()

    private val _countdown = MutableSharedFlow<Int>(0, 1, DROP_OLDEST)
    val countdown = _countdown.asSharedFlow()

    private val parentJob = SupervisorJob()

    private var recordJob: Job? = null

    private var startTime = 0L
    val dirname: String
        get() = startTime.toString()

    fun toggleRecord() {
        if (!isCameraBound) {
            return
        }
        if (_isRecording.value) {
            stopRecord()
        } else {
            startRecord()
        }
    }

    private fun startRecord() {
        _isRecording.value = true
        if (position == 0) {
            startTime = currentTimeInSeconds()
            CleanWorker.launch(context, dirname)
        }
        file = File(rootDir, "$dirname/${position + 1}.mp4")
    }

    fun stopRecord() {
        if (!isCameraBound) {
            return
        }
        clearRecord()
    }

    fun cancelRecord() {
        if (!isCameraBound) {
            return
        }
        if (_isRecording.value) {
            clearRecord()
            recordJob = null
        } else {
            position = max(0, position - 1)
        }
    }

    private fun clearRecord() {
        file = null
        if (recordJob != null) {
            recordJob?.cancel()
        } else {
            _isRecording.value = false
        }
    }

    fun onRecordEvent(event: VideoRecordEvent) {
        when (event) {
            is VideoRecordEvent.Start -> {
                recordJob = launch {
                    val seconds = preferences.duration.toInt()
                    (seconds downTo 1).forEach {
                        _countdown.emit(it)
                        delay(1000)
                    }
                }
                recordJob?.invokeOnCompletion {
                    file = null
                }
            }
            is VideoRecordEvent.Finalize -> {
                val options = event.outputOptions as FileOutputOptions
                when (event.error) {
                    ERROR_UNKNOWN, ERROR_ENCODING_FAILED, ERROR_RECORDER_ERROR, ERROR_NO_VALID_DATA -> {
                        // invalid file
                    }
                    else -> {
                        if (recordJob != null) {
                            if (options.file.exists()) {
                                position++
                            }
                        }
                    }
                }
                _isRecording.value = false
            }
            else -> {}
        }
    }

    fun clear() {
        parentJob.cancel()
    }

    override val coroutineContext = Dispatchers.Main + parentJob + CoroutineExceptionHandler { _, e ->
        Timber.e(e)
    }
}