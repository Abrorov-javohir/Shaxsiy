package com.automate123.videshorts.screen.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.automate123.videshorts.databinding.ActivityMainBinding
import com.automate123.videshorts.service.PermissionProvider
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var permProvider: PermissionProvider

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}