package com.automate123.videshorts.service

import androidx.camera.video.VideoRecordEvent
import com.automate123.videshorts.data.Preferences
import com.automate123.videshorts.extension.currentTimeInSeconds
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

@ViewModelScoped
class ShortsController @Inject constructor(
    private val preferences: Preferences,
    private val rootDir: File
) : CoroutineScope {

    private val _isRecording = MutableStateFlow(false)
    val isRecording = _isRecording.asStateFlow()

    private var inputFile: File? = null
        set(value) {
            field = value
            _recordFile.tryEmit(value)
        }
    private val _recordFile = MutableSharedFlow<File?>(0, 1, DROP_OLDEST)
    val recordFile = _recordFile.asSharedFlow()

    private val _countdown = MutableSharedFlow<Int>(0, 1, DROP_OLDEST)
    val countdown = _countdown.asSharedFlow()

    private val parentJob = SupervisorJob()

    private var recordJob: Job? = null

    @Volatile
    var isCameraBound = false

    var position = 0
        private set

    private var startTime = 0L
    val dirname: String
        get() = startTime.toString()

    fun toggleRecord() {
        if (!isCameraBound) {
            return
        }
        if (!_isRecording.value) {
            if (inputFile != null) {
                return
            }
            startRecord()
        } else {
            stopRecord()
        }
    }

    private fun startRecord() {
        recordJob?.cancel()
        position++
        if (position == 1) {
            startTime = currentTimeInSeconds()
        }
        inputFile = File(rootDir, "$dirname/$position.mp4")
    }

    private fun stopRecord() {
        recordJob?.cancel()
    }

    fun cancelRecord() {
        if (!isCameraBound) {
            return
        }
        val hasStarted = inputFile != null
        recordJob?.cancel()
        if (!hasStarted) {
            position--
        }
    }

    fun clearRecords() {
        if (!isCameraBound) {
            return
        }
        recordJob?.cancel()
        position = 0
    }

    fun onRecordEvent(event: VideoRecordEvent) {
        when (event) {
            is VideoRecordEvent.Start -> {
                _isRecording.value = true
                recordJob = launch {
                    val seconds = preferences.duration.toInt()
                    (seconds downTo 1).forEach {
                        _countdown.emit(it)
                        delay(1000)
                    }
                }
                recordJob?.invokeOnCompletion {
                    inputFile = null
                }
            }
            is VideoRecordEvent.Finalize -> {
                _isRecording.value = false
                recordJob?.cancel()
            }
            else -> {}
        }
    }

    override val coroutineContext = Dispatchers.Main + parentJob + CoroutineExceptionHandler { _, e ->
        Timber.e(e)
    }
}