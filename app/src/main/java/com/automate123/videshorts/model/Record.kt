package com.automate123.videshorts.model

import androidx.annotation.IntRange
import com.automate123.videshorts.MAX_SHORTS

class Record(
    val state: State,
    @IntRange(from = 0L, to = MAX_SHORTS.toLong())
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