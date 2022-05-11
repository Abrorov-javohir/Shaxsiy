package com.automate123.videshorts.screen.main

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import coil.load
import com.automate123.videshorts.KEY_DIRNAME
import com.automate123.videshorts.KEY_POSITION
import com.automate123.videshorts.R
import com.automate123.videshorts.databinding.FragmentControlsBinding
import com.automate123.videshorts.screen.preview.PreviewActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.jetbrains.anko.startActivity
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class ControlsFragment : Fragment() {

    @Inject
    lateinit var adapter: ThumbAdapter

    @Inject
    lateinit var rootDir: File

    private val viewModel: MainViewModel by activityViewModels()

    private lateinit var binding: FragmentControlsBinding

    private var accentColor = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val context = requireContext()
        accentColor = ContextCompat.getColor(context, R.color.colorAccent)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        binding = FragmentControlsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.rvThumbs.adapter = adapter
        binding.ivThumb.setOnClickListener {
            val context = requireContext()
            context.startActivity<PreviewActivity>(
                KEY_DIRNAME to viewModel.controller.dirname,
                KEY_POSITION to viewModel.controller.position
            )
        }
        binding.fab.setOnClickListener {
            viewModel.controller.toggleRecord()
        }
        binding.ivRetry.setOnClickListener {
            if (viewModel.controller.cancelRecord()) {
                updateAllInactive()
            }
        }
        binding.ivRetry.setOnLongClickListener {
            if (viewModel.controller.clearRecords()) {
                updateAllInactive()
            }
            true
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.controller.isRecording.collect { isRecording ->
                if (!isRecording) {
                    updateAllInactive()
                } else {
                    updateFab(true)
                }
            }
        }
    }

    private fun updateAllInactive() {
        updateAdapter()
        updateThumb()
        updateFab(false)
        updateRetry()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateAdapter() {
        adapter.apply {
            mDirname = viewModel.controller.dirname
            mPosition = viewModel.controller.position
            notifyDataSetChanged()
        }
    }

    private fun updateThumb() {
        val dirname = viewModel.controller.dirname
        val position = viewModel.controller.position
        binding.ivThumb.isEnabled = position > 0
        binding.ivThumb.load(File(rootDir, "$dirname/$position.mp4")) {
            error(null)
        }
    }

    private fun updateFab(isActive: Boolean) {
        binding.fab.apply {
            backgroundTintList = ColorStateList.valueOf(if (isActive) accentColor else Color.WHITE)
            imageTintList = ColorStateList.valueOf(if (isActive) Color.WHITE else accentColor)
            setImageResource(if (isActive) R.drawable.ic_baseline_stop_24 else R.drawable.ic_baseline_circle_24)
        }
    }

    private fun updateRetry() {
        val position = viewModel.controller.position
        binding.ivRetry.isEnabled = position > 0
    }
}