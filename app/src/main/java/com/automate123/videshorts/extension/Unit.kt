package com.automate123.videshorts.extension

import java.time.Instant

inline fun currentTimeInSeconds(): Long {
    return Instant.now().epochSecond
}