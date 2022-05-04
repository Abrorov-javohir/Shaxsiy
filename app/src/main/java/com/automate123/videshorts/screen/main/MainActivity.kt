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
import timber.log.Timber
import java.io.File

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        lifecycleScope.launch {
            viewModel.controller.output.collect {
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
}