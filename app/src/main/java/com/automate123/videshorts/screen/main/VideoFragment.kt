package com.automate123.videshorts.screen.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.automate123.videshorts.databinding.FragmentVideoBinding
import com.automate123.videshorts.service.CameraProvider
import com.automate123.videshorts.service.PermProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class VideoFragment : Fragment() {

    @Inject
    lateinit var permProvider: PermProvider

    @Inject
    lateinit var cameraProvider: CameraProvider

    private lateinit var binding: FragmentVideoBinding

    private lateinit var pCameraProvider: ProcessCameraProvider

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        binding = FragmentVideoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewLifecycleOwner.lifecycleScope.launch {
            permProvider.allGranted
                .filter { it }
                .collect {
                    pCameraProvider = cameraProvider.getInstance(requireContext())
                    startCamera()
                }
        }
    }

    private fun startCamera() {
        pCameraProvider.unbindAll()
        val preview = Preview.Builder()
            .build()
        preview.setSurfaceProvider(binding.preview.surfaceProvider)
        try {
            pCameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview)
        } catch (e: Throwable) {
            Timber.e(e)
        }
    }
}