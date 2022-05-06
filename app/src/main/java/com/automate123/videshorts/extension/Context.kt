package com.automate123.videshorts.extension

import android.content.Context
import androidx.core.content.PermissionChecker
import com.automate123.videshorts.BuildConfig
import java.io.File

val Context.rootDir: File
    get() = if (BuildConfig.DEBUG) externalCacheDir!! else cacheDir

fun Context.areGranted(vararg permissions: String): Boolean {
    return permissions.all { isGranted(it) }
}

fun Context.isGranted(permission: String): Boolean {
    return PermissionChecker.checkSelfPermission(applicationContext, permission) ==
        PermissionChecker.PERMISSION_GRANTED
}