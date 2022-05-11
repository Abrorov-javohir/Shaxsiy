package com.automate123.videshorts.screen.preview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.automate123.videshorts.databinding.FragmentLoadingBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoadingFragment : Fragment() {

    private val viewModel: PreviewViewModel by activityViewModels()

    private lateinit var binding: FragmentLoadingBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        binding = FragmentLoadingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.videoFile
                .drop(1)
                .collect {
                    binding.pbWait.isVisible = false
                    if (it != null) {
                        binding.tvWait.text = "Обработка прошла успешно"
                    } else {
                        binding.tvWait.text = "Ошибка при обработке"
                    }
                }
        }
    }
}