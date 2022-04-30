package com.automate123.videshorts.service

import com.automate123.videshorts.model.Record
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class ShortsController @Inject constructor() : CoroutineScope {

    private var position = 0

    val record = MutableStateFlow(Record(Record.State.ENDED, position))

    private val parentJob = SupervisorJob()

    override val coroutineContext = Dispatchers.Main + parentJob

    private var recordJob: Job? = null

    fun recordNext() {
        recordJob?.cancel()
        record.tryEmit(Record(Record.State.STARTING, ++position))
    }

    fun recordAgain() {
        recordJob?.cancel()
        record.tryEmit(Record(Record.State.STARTING, position))
    }

    fun onRecordStart() {
        recordJob = launch {
            record.emit(Record(Record.State.STARTED, position))
            delay(2000L)
            record.emit(Record(Record.State.ENDING, position))
        }
    }

    fun onRecordEnd() {
        record.tryEmit(Record(Record.State.ENDED, position))
    }
}