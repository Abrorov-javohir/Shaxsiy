package com.automate123.videshorts.service

import com.automate123.videshorts.model.Record
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class ShortsController @Inject constructor() : CoroutineScope {

    private val parentJob = SupervisorJob()

    override val coroutineContext = Dispatchers.Main + parentJob

    private var position = 0

    val record = MutableStateFlow(Record(Record.State.ENDED, position))

    private var waitJob: Job? = null

    fun recordNext() {
        position++
        recordAgain()
    }

    fun recordAgain() {
        waitJob?.cancel()
        record.tryEmit(Record(Record.State.STARTING, position))
    }

    fun onRecordStart() {
        record.tryEmit(Record(Record.State.STARTED, position))
        record()
    }

    private fun record() {
        waitJob = launch {
            delay(2000L)
            record.emit(Record(Record.State.ENDING, position))
        }
    }

    fun onRecordEnd() {
        record.tryEmit(Record(Record.State.ENDED, position))
    }
}