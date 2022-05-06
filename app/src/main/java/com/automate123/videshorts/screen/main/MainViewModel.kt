package com.automate123.videshorts.screen.main

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import com.automate123.videshorts.service.ShortsController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val controller: ShortsController
) : ViewModel() {

    val preview = MutableSharedFlow<Bitmap?>()
}