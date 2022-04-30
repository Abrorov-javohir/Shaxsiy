package com.automate123.videshorts.service

import com.automate123.videshorts.model.Record
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class ShortsController @Inject constructor() : CoroutineScope {

    val record = MutableSharedFlow<Record>()

    private val parentJob = SupervisorJob()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main

    fun recordNext() {

    }

    fun recordAgain() {

    }
}