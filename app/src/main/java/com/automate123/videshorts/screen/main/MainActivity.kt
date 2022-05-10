package com.automate123.videshorts.screen.main

import android.animation.Animator
import android.animation.AnimatorInflater
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.automate123.videshorts.R
import com.automate123.videshorts.databinding.ActivityMainBinding
import com.automate123.videshorts.screen.settings.SettingsActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.jetbrains.anko.startActivity

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    private lateinit var binding: ActivityMainBinding

    private lateinit var countdown: Animator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        countdown = AnimatorInflater.loadAnimator(applicationContext, R.animator.countdown)
        countdown.setTarget(binding.tvTime)
        binding.ivSettings.setOnClickListener {
            viewModel.controller.stopRecord()
            startActivity<SettingsActivity>()
        }
        lifecycleScope.launch {
            viewModel.controller.countdown.collect {
                countdown.cancel()
                binding.tvTime.text = it.toString()
                countdown.start()
            }
        }
    }

    override fun onDestroy() {
        countdown.cancel()
        super.onDestroy()
    }
}