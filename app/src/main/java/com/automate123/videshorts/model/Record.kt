package com.automate123.videshorts.model

import androidx.annotation.IntRange
import com.automate123.videshorts.MAX_SHORTS

class Record(
    val state: State = State.NONE,
    @IntRange(from = 1L, to = MAX_SHORTS.toLong())
    val position: Int = 1,
    val isFinalState: Boolean = true
) {

    enum class State {
        NONE,
        START,
        END,
        FINISH
    }
}