package com.automate123.videshorts.extension

import android.graphics.drawable.BitmapDrawable
import android.widget.ImageView

val ImageView.hasImage: Boolean
    get() {
        val image = drawable ?: return false
        if (image is BitmapDrawable) {
            return image.bitmap != null
        }
        return true
    }