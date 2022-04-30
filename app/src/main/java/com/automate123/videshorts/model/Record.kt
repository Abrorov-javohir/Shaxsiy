package com.automate123.videshorts.model

import androidx.annotation.IntRange
import com.automate123.videshorts.SHORTS_COUNT

class Record(
    val state: State,
    @IntRange(from = 0L, to = SHORTS_COUNT.toLong())
    val position: Int
) {

    constructor() : this(State.ENDED, 0)

    enum class State {
        STARTING,
        STARTED,
        ENDING,
        ENDED
    }
}