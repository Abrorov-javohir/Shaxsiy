package com.automate123.videshorts.extension

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

fun Context.areGranted(vararg permissions: String): Boolean {
    return permissions.all { isGranted(it) }
}

fun Context.isGranted(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(applicationContext, permission) ==
        PackageManager.PERMISSION_GRANTED
}