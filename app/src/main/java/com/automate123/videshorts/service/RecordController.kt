package com.automate123.videshorts.service

import android.content.Context
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.VideoRecordEvent
import androidx.work.WorkInfo
import com.automate123.videshorts.EXTRA_FILENAME
import com.automate123.videshorts.MAX_SHORTS
import com.automate123.videshorts.extension.asFlow
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
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

    private val _isBusy = MutableStateFlow(false)
    val isCapturing = _isBusy.asStateFlow()

    private val _currentPosition = MutableStateFlow(0)
    val currentPosition = _currentPosition.asStateFlow()
    private var position = 0
        set(value) {
            field = value
            _currentPosition.tryEmit(value)
        }

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

    fun recordNext() {
        if (!isCameraBound) {
            return
        }
        captureJob?.cancel()

        position++
        _inputOptions.tryEmit(getOutputOptions())
    }

    fun recordAgain() {
        if (!isCameraBound) {
            return
        }
        captureJob?.cancel()
        _inputOptions.tryEmit(getOutputOptions())
    }

    fun onRecordEvent(event: VideoRecordEvent) {
        when (event) {
            is VideoRecordEvent.Start -> {
                captureJob = launch {
                    _isBusy.emit(true)
                    delay(2000L)
                    _inputOptions.emit(null)
                }
            }
            is VideoRecordEvent.Finalize -> {
                _isBusy.tryEmit(false)
                if (position >= MAX_SHORTS) {
                    processJob = launch {
                        val workDir = File(rootDir, startTime.toString())
                        VideoWorker.launch(context, workDir.name)
                            .asFlow()
                            .collect {
                                when (it.state) {
                                    WorkInfo.State.SUCCEEDED -> {
                                        _outputFile.emit(info.outputData.getString(EXTRA_FILENAME))
                                    }
                                    WorkInfo.State.FAILED -> throw Throwable()
                                    else -> {}
                                }
                            }
                    }
                }
            }
            else -> {}
        }
    }

    private fun getOutputOptions(): FileOutputOptions {
        return FileOutputOptions
            .Builder(File(workDir, "$position.mp4"))
            .build()
    }
}