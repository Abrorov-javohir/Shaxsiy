package com.automate123.videshorts.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import dagger.hilt.android.qualifiers.ActivityContext
import javax.inject.Inject

class PermissionProvider @Inject constructor(
    @ActivityContext private val context: Context
) : DefaultLifecycleObserver {

    lateinit var launcher: ActivityResultLauncher<Array<String>>

    init {
        (context as? AppCompatActivity)?.let {
            context.lifecycle.addObserver(this)
        }
    }

    override fun onCreate(owner: LifecycleOwner) {
        context as AppCompatActivity
        launcher = context.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {

        }
        requestNonGranted()
    }

    @UiThread
    fun requestNonGranted() {
        val nonGranted = permissions.filter { !isGranted(it) }
        if (nonGranted.isNotEmpty()) {
            launcher.launch(nonGranted.toTypedArray())
        }
    }

    fun areGranted(): Boolean {
        return permissions.all { isGranted(it) }
    }

    fun isGranted(permission: String): Boolean {
        return context.packageManager.checkPermission(permission, context.packageName) ==
            PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy(owner: LifecycleOwner) {
        (context as? AppCompatActivity)?.let {
            context.lifecycle.removeObserver(this)
        }
    }

    companion object {

        private val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
    }
}