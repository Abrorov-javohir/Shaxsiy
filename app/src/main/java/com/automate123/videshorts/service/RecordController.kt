package com.automate123.videshorts.service

import android.content.Context
import androidx.camera.video.VideoRecordEvent
import com.automate123.videshorts.MAX_SHORTS
import com.automate123.videshorts.model.Record
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class RecordController @Inject constructor(
    @ApplicationContext private val context: Context
) : CoroutineScope {

    private val _record = MutableStateFlow(Record())
    val record: StateFlow<Record>
        get() = _record.asStateFlow()

    private var position = 0

    private val parentJob = SupervisorJob()

    override val coroutineContext = Dispatchers.Main + parentJob

    private var recordJob: Job? = null

    fun recordNext() {
        recordJob?.cancel()

        _record.tryEmit(Record(Record.State.START, ++position))
    }

    fun recordAgain() {
        recordJob?.cancel()
        _record.tryEmit(Record(Record.State.START, position))
    }

    fun onRecordEvent(event: VideoRecordEvent) {
        when (event) {
            is VideoRecordEvent.Start -> {
                recordJob = launch {
                    _record.emit(Record(Record.State.START, position))
                    delay(2000L)
                    _record.emit(Record(Record.State.END, position))
                }
            }
            is VideoRecordEvent.Pause -> {
                _record.tryEmit(Record(Record.State.PAUSE, position))
            }
            is VideoRecordEvent.Resume -> {
                _record.tryEmit(Record(Record.State.RESUME, position))
            }
            is VideoRecordEvent.Finalize -> {
                if (position >= MAX_SHORTS) {
                    _record.tryEmit(Record(Record.State.FINISH, position, false))
                    VideoWorker.launch(context)
                } else {
                    _record.tryEmit(Record(Record.State.END, position))
                }
            }
            else -> {}
        }
    }
}