package com.automate123.videshorts.screen.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.automate123.videshorts.KEY_DIRNAME
import com.automate123.videshorts.KEY_POSITION
import com.automate123.videshorts.databinding.FragmentControlsBinding
import com.automate123.videshorts.screen.preview.PreviewActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.filter
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        binding = FragmentControlsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.rvThumbs.adapter = adapter
        binding.ivThumb.setOnClickListener {
            val context = requireContext()
            val dirname = viewModel.controller.dirname
            val position = viewModel.controller.recordPosition.value
            context.startActivity<PreviewActivity>(
                KEY_DIRNAME to dirname,
                KEY_POSITION to position
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
            viewModel.controller.recordPosition
                .filter { it == 0 }
                .collect {
                    binding.ivThumb.isEnabled = false
                    binding.ivThumb.setImageDrawable(null)
                }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.controller.isRecording.collect { isRecording ->
                val position = viewModel.controller.recordPosition.value
                if (isRecording) {
                    binding.fab.isEnabled = false
                    binding.ivRetry.isEnabled = true
                } else {
                    adapter.dirname = count
                    adapter.count = count
                    adapter.notifyDataSetChanged()
                    binding.ivThumb.load(File(rootDir, "$dirname/$position.mp4"))
                    binding.fab.isEnabled = true
                    binding.ivRetry.isEnabled = position > 0
                }
            }
        }
    }
}