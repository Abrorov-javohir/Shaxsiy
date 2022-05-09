package com.automate123.videshorts.screen.preview

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.exoplayer.ExoPlayer
import com.automate123.videshorts.databinding.FragmentPlayerBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlayerFragment : Fragment() {

    private lateinit var player: ExoPlayer

    private lateinit var binding: FragmentPlayerBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        binding = FragmentPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initPlayer()
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun initPlayer() {
        val context = requireContext()
        player = ExoPlayer.Builder(context)
            .build()
        binding.video.player = player
    }

    private fun startPlay() {
        val item = MediaItem.Builder()
            .setUri("")
            .setMimeType(MimeTypes.APPLICATION_MP4)
            .build()
        player.setMediaItem(item)
        player.playWhenReady = true
        player.prepare()
    }

    private fun releasePlayer() {
        player.release()
    }

    override fun onDestroyView() {
        releasePlayer()
        super.onDestroyView()
    }
}