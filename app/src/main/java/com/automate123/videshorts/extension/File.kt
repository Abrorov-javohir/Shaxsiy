package com.automate123.videshorts.extension

import java.io.File

val File.qPath: String
    get() = "'$path'"