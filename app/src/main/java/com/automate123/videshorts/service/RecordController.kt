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

    private val _isCapturing = MutableStateFlow(false)
    val isCapturing: StateFlow<Boolean>
        get() = _isCapturing.asStateFlow()

    private val _position = MutableStateFlow(0)
    val position: StateFlow<Int>
        get() = _position.asStateFlow()

    private val _inputOptions = MutableSharedFlow<FileOutputOptions?>()
    val inputOptions: SharedFlow<FileOutputOptions?>
        get() = _inputOptions.asSharedFlow()

    private val _outputFile = MutableSharedFlow<File>()
    val outputFile: SharedFlow<File>
        get() = _outputFile.asSharedFlow()

    private var startTime = Instant.now().epochSecond

    private val parentJob = SupervisorJob()

    private var captureJob: Job? = null

    private var processJob: Job? = null

    override val coroutineContext = Dispatchers.Main + parentJob + CoroutineExceptionHandler { _, e ->
        Timber.e(e)
    }

    fun recordNext() {
        if (!isCameraBound) {
            return
        }
        captureJob?.cancel()

        position++
        _inputOptions.tryEmit(fileManager.getOutputOptions(_position))
    }

    fun recordAgain() {
        if (!isCameraBound) {
            return
        }
        captureJob?.cancel()
        _inputOptions.tryEmit(FileOutputOptions.Builder(File(workDir, "$position.mp4"))
            .build())
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
}