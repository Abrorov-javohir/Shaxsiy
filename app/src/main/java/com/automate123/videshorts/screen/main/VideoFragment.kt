package com.automate123.videshorts.screen.main

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.automate123.videshorts.data.FileManager
import com.automate123.videshorts.databinding.FragmentVideoBinding
import com.automate123.videshorts.extension.isGranted
import com.automate123.videshorts.model.Record
import com.automate123.videshorts.service.CameraProvider
import com.automate123.videshorts.service.PermProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class VideoFragment : Fragment() {

    @Inject
    lateinit var permProvider: PermProvider

    @Inject
    lateinit var fileManager: FileManager

    private val viewModel: MainViewModel by activityViewModels()

    private lateinit var binding: FragmentVideoBinding

    private lateinit var cameraProvider: ProcessCameraProvider

    private lateinit var videoCapture: VideoCapture<Recorder>

    private var recording: Recording? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        binding = FragmentVideoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewLifecycleOwner.lifecycleScope.launch {
            permProvider.grantedPerms
                .filter { it.contains(Manifest.permission.CAMERA) }
                .collect {
                    cameraProvider = CameraProvider.getInstance(requireContext())
                    startCamera()
                }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.controller.record
                .filterNot { it.isFinalState }
                .collect {
                    when (it.state) {
                        Record.State.START -> {
                            startRecording(it.position)
                        }
                        Record.State.END -> {
                            stopRecording()
                        }
                        else -> {}
                    }
                }
        }
    }

    private fun startCamera() {
        cameraProvider.unbindAll()

        val preview = Preview.Builder()
            .build()
        preview.setSurfaceProvider(binding.preview.surfaceProvider)

        val recorder = Recorder.Builder()
            .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
            .build()
        videoCapture = VideoCapture.withOutput(recorder)

        try {
            cameraProvider.bindToLifecycle(
                this,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                videoCapture
            )
        } catch (e: Throwable) {
            Timber.e(e)
        }
    }

    @SuppressLint("MissingPermission")
    private fun startRecording(position: Int) {
        val context = requireContext()
        stopRecording()
        recording = videoCapture.output
            .prepareRecording(context, fileManager.getOutputOptions(position))
            .apply {
                if (context.isGranted(Manifest.permission.RECORD_AUDIO)) {
                    withAudioEnabled()
                }
            }
            .start(ContextCompat.getMainExecutor(context)) {
                viewModel.controller.onRecordEvent(it)
            }
    }

    private fun stopRecording() {
        recording?.stop()
        recording = null
    }

    override fun onStop() {
        stopRecording()
        super.onStop()
    }

    override fun onDestroyView() {
        if (::cameraProvider.isInitialized) {
            cameraProvider.unbindAll()
        }
        super.onDestroyView()
    }
}