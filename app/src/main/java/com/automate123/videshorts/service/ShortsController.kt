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

    private var file: File? = null
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
        if (_isRecording.value) {
            stopRecord()
        } else {
            startRecord()
        }
    }

    private fun startRecord() {
        if (file != null) {
            return
        }
        clearRecord()
        position++
        if (position == 1) {
            startTime = currentTimeInSeconds()
        }
        file = File(rootDir, "$dirname/$position.mp4")
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
        val isActive = file != null || _isRecording.value
        clearRecord()
        if (!isActive) {
            position--
        }
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
                    clearRecord()
                }
            }
            is VideoRecordEvent.Finalize -> {
                _isRecording.value = false
                clearRecord()
            }
            else -> {}
        }
    }

    private fun clearRecord() {
        recordJob?.cancel()
        file = null
    }

    override val coroutineContext = Dispatchers.Main + parentJob + CoroutineExceptionHandler { _, e ->
        Timber.e(e)
    }
}