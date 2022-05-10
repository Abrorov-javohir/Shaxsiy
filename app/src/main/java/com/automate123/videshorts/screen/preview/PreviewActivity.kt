package com.automate123.videshorts.screen.preview

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.work.WorkInfo
import com.automate123.videshorts.KEY_DIRNAME
import com.automate123.videshorts.KEY_FILENAME
import com.automate123.videshorts.KEY_POSITION
import com.automate123.videshorts.R
import com.automate123.videshorts.databinding.ActivityPreviewBinding
import com.automate123.videshorts.extension.asFlow
import com.automate123.videshorts.screen.BaseActivity
import com.automate123.videshorts.service.VideoWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class PreviewActivity : BaseActivity() {

    @Inject
    lateinit var rootDir: File

    private val viewModel: PreviewViewModel by viewModels()

    private lateinit var binding: ActivityPreviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        lifecycleScope.launch {
            try {
                val dirname = intent.getStringExtra(KEY_DIRNAME)!!
                val position = intent.getIntExtra(KEY_POSITION, 0)
                VideoWorker.launch(applicationContext, dirname, position)
                    .asFlow()
                    .collect {
                        when (it.state) {
                            WorkInfo.State.SUCCEEDED -> {
                                val filename = it.outputData.getString(KEY_FILENAME)!!
                                viewModel.videoFile.value = File(rootDir, "$dirname/$filename")
                                invalidateOptionsMenu()
                            }
                            WorkInfo.State.CANCELLED -> throw CancellationException()
                            WorkInfo.State.FAILED -> throw RuntimeException()
                            else -> {}
                        }
                    }
            } catch (e: Throwable) {
                Timber.e(e)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_preview, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(R.id.action_share)?.isEnabled = viewModel.videoFile.value != null
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_share -> {
                viewModel.videoFile.value?.let {
                    shareResult(it)
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
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