package com.automate123.videshorts.screen.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.automate123.videshorts.databinding.FragmentControlsBinding
import com.automate123.videshorts.model.Record
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
            viewModel.controller.recordAgain()
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.controller.record.collect {
                when (it.state) {
                    Record.State.STARTING -> {
                        binding.fabForward.isEnabled = false
                        binding.ivRetry.isEnabled = false
                    }
                    Record.State.STARTED -> {
                        binding.fabForward.isEnabled = false
                        binding.ivRetry.isEnabled = true
                    }
                    Record.State.ENDING -> {
                        binding.fabForward.isEnabled = false
                        binding.ivRetry.isEnabled = false
                    }
                    Record.State.ENDED -> {
                        binding.fabForward.isEnabled = true
                        binding.ivRetry.isEnabled = it.position > 0
                        // todo loading thumb
                    }
                }
            }
        }
    }
}