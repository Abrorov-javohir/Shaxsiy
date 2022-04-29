package com.automate123.videshorts.screen.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.automate123.videshorts.databinding.ActivityMainBinding
import com.automate123.videshorts.service.PermProvider
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var permProvider: PermProvider

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}