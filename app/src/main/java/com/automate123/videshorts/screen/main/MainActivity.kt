package com.automate123.videshorts.screen.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.automate123.videshorts.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.jetbrains.anko.indeterminateProgressDialog
import timber.log.Timber
import java.io.File

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val waitDialogDelegate = lazy { indeterminateProgressDialog("Идет обработка...", "Подождите") }
    private val waitDialog by waitDialogDelegate

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        lifecycleScope.launch {
            viewModel.controller.currentPosition.collect {
                binding.tvPosition.text = if (it > 0) it.toString() else null
            }
        }
        lifecycleScope.launch {
            viewModel.controller.isProcessing.collect {
                if (it) {
                    waitDialog.show()
                } else if (waitDialogDelegate.isInitialized()) {
                    waitDialog.dismiss()
                }
            }
        }
        lifecycleScope.launch {
            viewModel.controller.outputFile.collect {
                shareResult(it)
            }
        }
    }

    private fun shareResult(file: File) {
        try {
            val uri = FileProvider.getUriForFile(applicationContext, "$packageName.file_provider", file)
            startActivity(ShareCompat.IntentBuilder(applicationContext)
                .setType("video/mp4")
                .setStream(uri)
                .intent
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION))
        } catch (e: Throwable) {
            Timber.e(e)
        }
    }

    override fun onDestroy() {
        if (waitDialogDelegate.isInitialized()) {
            waitDialog.dismiss()
        }
        super.onDestroy()
    }
}