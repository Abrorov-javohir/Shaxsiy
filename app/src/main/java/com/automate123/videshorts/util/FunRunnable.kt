package com.automate123.videshorts.util

class FunRunnable(private val body: () -> Unit) {

    var isLocked = false
        private set

    fun run() {
        body.invoke()
    }

    fun tryRun() {
        if (!isLocked) {
            isLocked = true
            run()
        }
    }

    fun unlock() {
        isLocked = false
    }
}