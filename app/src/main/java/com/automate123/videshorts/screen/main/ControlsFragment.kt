package com.automate123.videshorts.screen.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.automate123.videshorts.databinding.FragmentControlsBinding
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class ControlsFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()

    private lateinit var binding: FragmentControlsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        binding = FragmentControlsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.fabForward.setOnClickListener {
            viewModel.controller.recordNext()
        }
        binding.ivRetry.setOnClickListener {
            viewModel.controller.repeatAgain()
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.preview.collect {
                binding.ivThumb.setImageBitmap(it)
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            combine(
                viewModel.controller.currentPosition,
                viewModel.controller.isRecording,
                viewModel.controller.isProcessing
            ) { _, isRecording, isProcessing ->
                isRecording || isProcessing
            }.collect { isBusy ->
                val position = viewModel.controller.currentPosition.value
                if (isBusy) {
                    binding.fabForward.isEnabled = false
                    binding.ivRetry.isEnabled = true
                } else {
                    binding.fabForward.isEnabled = true
                    binding.ivRetry.isEnabled = position > 0
                }
            }
        }
    }
}