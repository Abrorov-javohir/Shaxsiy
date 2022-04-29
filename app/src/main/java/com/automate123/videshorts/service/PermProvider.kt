package com.automate123.videshorts.service

import android.Manifest
import android.content.Context
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.automate123.videshorts.extension.areGranted
import com.automate123.videshorts.extension.isGranted
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Inject

@ActivityScoped
class PermProvider @Inject constructor(
    @ActivityContext private val context: Context
) : DefaultLifecycleObserver {

    val allGranted = MutableSharedFlow<Boolean>(1)

    private lateinit var launcher: ActivityResultLauncher<Array<String>>

    init {
        (context as? AppCompatActivity)?.let {
            context.lifecycle.addObserver(this)
        }
    }

    override fun onCreate(owner: LifecycleOwner) {
        context as AppCompatActivity
        launcher = context.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            allGranted.tryEmit(context.areGranted(*permissions))
        }
        allGranted.tryEmit(context.areGranted(*permissions))
        requestNonGranted()
    }

    @UiThread
    fun requestNonGranted() {
        val nonGranted = permissions.filterNot { context.isGranted(it) }
        if (nonGranted.isNotEmpty()) {
            launcher.launch(nonGranted.toTypedArray())
        }
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