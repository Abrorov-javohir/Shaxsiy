package com.automate123.videshorts.extension

import java.io.File

inline val File.qPath: String
    get() = "'$path'"