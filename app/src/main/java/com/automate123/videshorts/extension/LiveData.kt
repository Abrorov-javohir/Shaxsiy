package com.automate123.videshorts.extension

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn

fun <T> LiveData<T>.asFlow(): Flow<T> = callbackFlow {
    val observer = Observer<T> {
        trySend(it)
    }
    observeForever(observer)
    awaitClose {
        removeObserver(observer)
    }
}.flowOn(Dispatchers.Main.immediate)