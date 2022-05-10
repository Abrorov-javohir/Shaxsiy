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
                KEY_POSITION to viewModel.controller.recordPosition.value
            )
        }
        binding.fab.setOnClickListener {
            viewModel.controller.toggleRecord()
        }
        binding.ivRetry.setOnClickListener {
            viewModel.controller.cancelRecord()
        }
        binding.ivRetry.setOnLongClickListener {
            viewModel.controller.clearRecords()
            true
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.controller.recordPosition.collect { position ->
                updateAdapter()
                if (position <= 0) {
                    binding.ivThumb.isEnabled = false
                    binding.ivThumb.setImageDrawable(null)
                }
                binding.ivRetry.isEnabled = position > 0
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.preview.collect {
                binding.ivThumb.isEnabled = true
                binding.ivThumb.setImageBitmap(it)
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.controller.isRecording.collect { isRecording ->
                if (isRecording) {
                    binding.fab.backgroundTintList = ColorStateList.valueOf(accentColor)
                    binding.fab.imageTintList = ColorStateList.valueOf(Color.WHITE)
                    binding.fab.setImageResource(R.drawable.ic_baseline_stop_24)
                } else {
                    updateAdapter()
                    binding.fab.backgroundTintList = ColorStateList.valueOf(Color.WHITE)
                    binding.fab.imageTintList = ColorStateList.valueOf(accentColor)
                    binding.fab.setImageResource(R.drawable.ic_baseline_circle_24)
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateAdapter() {
        adapter.apply {
            dirname = viewModel.controller.dirname
            count = viewModel.controller.recordPosition.value
            notifyDataSetChanged()
        }
    }
}