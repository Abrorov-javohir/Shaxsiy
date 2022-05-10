package com.automate123.videshorts.screen.preview

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.exoplayer.ExoPlayer
import com.automate123.videshorts.databinding.FragmentPlayerBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import java.io.File

@AndroidEntryPoint
class PlayerFragment : Fragment() {

    private val viewModel: PreviewViewModel by activityViewModels()

    private lateinit var exoPlayer: ExoPlayer

    private lateinit var binding: FragmentPlayerBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        binding = FragmentPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initPlayer()
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.videoFile
                .filterNotNull()
                .collect {
                    binding.video.isVisible = true
                    preparePlay(it)
                }
        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun initPlayer() {
        val context = requireContext()
        exoPlayer = ExoPlayer.Builder(context)
            .build()
        binding.video.apply {
            player = exoPlayer
            controllerAutoShow = false
            controllerShowTimeoutMs = 2000
        }
    }

    private fun preparePlay(file: File) {
        val item = MediaItem.Builder()
            .setUri(Uri.fromFile(file))
            .setMimeType(MimeTypes.APPLICATION_MP4)
            .build()
        exoPlayer.setMediaItem(item)
        exoPlayer.playWhenReady = true
        exoPlayer.prepare()
    }

    private fun releasePlayer() {
        exoPlayer.release()
    }

    override fun onDestroyView() {
        releasePlayer()
        super.onDestroyView()
    }
}