package com.automate123.videshorts.service

import android.content.Context
import androidx.camera.video.VideoRecordEvent
import androidx.work.WorkInfo
import com.automate123.videshorts.KEY_FILENAME
import com.automate123.videshorts.MAX_SHORTS
import com.automate123.videshorts.extension.asFlow
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

@ViewModelScoped
class ShortsController @Inject constructor(
    @ApplicationContext private val context: Context,
    private val rootDir: File
) : CoroutineScope {

    @Volatile
    var isCameraBound = false

    private val _isRecording = MutableStateFlow(false)
    val isRecording = _isRecording.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing = _isProcessing.asStateFlow()

    private var position = 0
        set(value) {
            field = value
            _currentPosition.tryEmit(value)
        }
    private val _currentPosition = MutableStateFlow(position)
    val currentPosition = _currentPosition.asStateFlow()

    private val _inputFile = MutableSharedFlow<File?>(0, 1, DROP_OLDEST)
    val inputFile = _inputFile.asSharedFlow()

    private val _outputFile = MutableSharedFlow<File>(0, 1, DROP_OLDEST)
    val outputFile = _outputFile.asSharedFlow()

    private var startTime = currentTimeInSeconds()

    private val parentJob = SupervisorJob()

    private var recordJob: Job? = null

    private var processJob: Job? = null

    private val workDir: File
        get() = File(rootDir, startTime.toString())

    private val recordFile: File
        get() = File(workDir, "$position.mp4")

    fun recordNext() {
        if (!isCameraBound) {
            return
        }
        parentJob.cancelChildren()
        if (position >= MAX_SHORTS) {
            position = 1
        } else {
            position++
        }
        if (position == 1) {
            startTime = currentTimeInSeconds()
        }
        _inputFile.tryEmit(recordFile)
    }

    fun repeatAgain() {
        if (!isCameraBound) {
            return
        }
        parentJob.cancelChildren()
        _inputFile.tryEmit(recordFile)
    }

    fun onRecordEvent(event: VideoRecordEvent) {
        when (event) {
            is VideoRecordEvent.Start -> {
                _isRecording.tryEmit(true)
                recordJob = launch {
                    delay(2000L)
                    _inputFile.emit(null)
                }
            }
            is VideoRecordEvent.Finalize -> {
                _isRecording.tryEmit(false)
                if (position >= MAX_SHORTS) {
                    position = 0
                    processJob = launch {
                        _isProcessing.emit(true)
                        VideoWorker.launch(context, workDir.name)
                            .asFlow()
                            .collect {
                                when (it.state) {
                                    WorkInfo.State.SUCCEEDED -> {
                                        val filename = it.outputData.getString(KEY_FILENAME)!!
                                        _outputFile.emit(File(workDir, filename))
                                        processJob?.cancel()
                                    }
                                    WorkInfo.State.CANCELLED -> throw CancellationException()
                                    WorkInfo.State.FAILED -> throw RuntimeException()
                                    else -> {}
                                }
                            }
                    }
                    processJob?.invokeOnCompletion {
                        VideoWorker.cancel(context)
                        _isProcessing.tryEmit(false)
                    }
                }
            }
            else -> {}
        }
    }

    override val coroutineContext = Dispatchers.Main + parentJob + CoroutineExceptionHandler { _, e ->
        Timber.e(e)
    }
}