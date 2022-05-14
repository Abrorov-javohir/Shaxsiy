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
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerColorDrawable
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
            viewModel.controller.stopRecord()
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
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.controller.isRecording.collect { isRecording ->
                updateFab(isRecording)
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.controller.recordPosition.collect { position ->
                updateAdapter(position)
                updateThumb(position)
                updateRetry(position)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateAdapter(position: Int) {
        adapter.apply {
            mDirname = viewModel.controller.dirname
            mPosition = position
            notifyDataSetChanged()
        }
    }

    private fun updateThumb(position: Int) {
        val dirname = viewModel.controller.dirname
        binding.ivThumb.isEnabled = position > 0
        if (position > 0) {
            binding.ivThumb.load(File(rootDir, "$dirname/$position.mp4")) {
                placeholder(ShimmerColorDrawable(Color.WHITE).apply {
                    setShimmer(Shimmer.ColorHighlightBuilder().build())
                })
                error(null)
            }
        } else {
            binding.ivThumb.setImageDrawable(null)
        }
    }

    private fun updateFab(isRecording: Boolean) {
        binding.fab.apply {
            backgroundTintList = ColorStateList.valueOf(if (isRecording) accentColor else Color.WHITE)
            imageTintList = ColorStateList.valueOf(if (isRecording) Color.WHITE else accentColor)
            setImageResource(if (isRecording) R.drawable.ic_baseline_stop_24 else R.drawable.ic_baseline_circle_24)
        }
    }

    private fun updateRetry(position: Int) {
        binding.ivRetry.isEnabled = position > 0
    }
}