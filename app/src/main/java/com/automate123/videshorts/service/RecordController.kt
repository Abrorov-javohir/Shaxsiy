package com.automate123.videshorts.service

import android.content.Context
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.VideoRecordEvent
import androidx.work.WorkInfo
import com.automate123.videshorts.KEY_FILENAME
import com.automate123.videshorts.MAX_SHORTS
import com.automate123.videshorts.extension.asFlow
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import java.io.File
import java.time.Instant
import javax.inject.Inject

@ViewModelScoped
class RecordController @Inject constructor(
    @ApplicationContext private val context: Context,
    private val rootDir: File
) : CoroutineScope {

    @Volatile
    var isCameraBound = false

    private val _isCapturing = MutableStateFlow(false)
    val isCapturing = _isCapturing.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing = _isProcessing.asStateFlow()

    private var position = 0
        set(value) {
            field = value
            _currentPosition.tryEmit(value)
        }
    private val _currentPosition = MutableStateFlow(position)
    val currentPosition = _currentPosition.asStateFlow()

    private val _inputOptions = MutableSharedFlow<FileOutputOptions?>()
    val inputOptions = _inputOptions.asSharedFlow()

    private val _outputFile = MutableSharedFlow<File>()
    val outputFile = _outputFile.asSharedFlow()

    private var startTime = Instant.now().epochSecond

    private val parentJob = SupervisorJob()

    private var captureJob: Job? = null

    private var processJob: Job? = null

    override val coroutineContext = Dispatchers.Main + parentJob + CoroutineExceptionHandler { _, e ->
        Timber.e(e)
    }

    private val workDir: File
        get() = File(rootDir, startTime.toString())

    private val currentOptions: FileOutputOptions
        get() = FileOutputOptions.Builder(File(workDir, "$position.mp4"))
            .build()

    fun recordNext() {
        if (!isCameraBound) {
            return
        }
        parentJob.cancelChildren()

        position++
        _inputOptions.tryEmit(currentOptions)
    }

    fun recordAgain() {
        if (!isCameraBound) {
            return
        }
        parentJob.cancelChildren()
        _inputOptions.tryEmit(currentOptions)
    }

    fun onRecordEvent(event: VideoRecordEvent) {
        when (event) {
            is VideoRecordEvent.Start -> {
                captureJob = launch {
                    _isCapturing.emit(true)
                    delay(2000L)
                    _inputOptions.emit(null)
                }
            }
            is VideoRecordEvent.Finalize -> {
                _isCapturing.tryEmit(false)
                if (position >= MAX_SHORTS) {
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
}