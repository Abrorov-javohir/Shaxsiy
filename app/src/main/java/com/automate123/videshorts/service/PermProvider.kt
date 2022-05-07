package com.automate123.videshorts.service

import android.Manifest
import android.content.Context
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.automate123.videshorts.extension.isGranted
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject

@ActivityScoped
class PermProvider @Inject constructor(
    @ActivityContext private val context: Context
) : DefaultLifecycleObserver {

    private val _grantedPerms = MutableSharedFlow<List<String>>(1, 0, DROP_OLDEST)
    val grantedPerms: SharedFlow<List<String>>
        get() = _grantedPerms.asSharedFlow()

    private lateinit var launcher: ActivityResultLauncher<Array<String>>

    init {
        context as AppCompatActivity
        context.lifecycle.addObserver(this)
    }

    override fun onCreate(owner: LifecycleOwner) {
        context as AppCompatActivity
        launcher = context.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            emitGranted()
        }
        emitGranted()
        requestNonGranted()
    }

    @UiThread
    fun requestNonGranted() {
        val notGranted = dangerPerms.filterNot { context.isGranted(it) }
        if (notGranted.isNotEmpty()) {
            launcher.launch(notGranted.toTypedArray())
        }
    }

    private fun emitGranted() {
        _grantedPerms.tryEmit(dangerPerms.filter { context.isGranted(it) })
    }

    override fun onDestroy(owner: LifecycleOwner) {
        context as AppCompatActivity
        context.lifecycle.removeObserver(this)
    }

    companion object {

        private val dangerPerms = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
    }
}