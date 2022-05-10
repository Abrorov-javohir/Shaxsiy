package com.automate123.videshorts.screen.main

import android.content.Context
import android.view.ViewGroup
import android.widget.Space
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.request.videoFrameMillis
import com.automate123.videshorts.databinding.ItemThumbBinding
import dagger.hilt.android.qualifiers.ApplicationContext
import org.jetbrains.anko.dip
import org.jetbrains.anko.layoutInflater
import org.jetbrains.anko.matchParent
import java.io.File
import javax.inject.Inject
import kotlin.math.max

class ThumbAdapter @Inject constructor(
    @ApplicationContext context: Context,
    private val rootDir: File
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var dirname: String? = null

    var count = 0
        set(value) {
            field = max(minCount, value)
        }

    private var minCount = 0
        set(value) {
            field = value
            count = value
        }

    init {
        with(context) {
            while (true) {
                val position = minCount + 1
                val id = resources.getIdentifier("ic_$position", "drawable", packageName)
                if (id == 0) {
                    break
                }
                minCount++
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            0 -> SpaceHolder(parent.context)
            else -> {
                val binding = ItemThumbBinding
                    .inflate(parent.context.layoutInflater, parent, false)
                ItemHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemHolder) {
            holder.bindView(position)
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (position > count) {
            return 0
        }
        return position
    }

    override fun getItemCount() = 1 + count + 1

    class SpaceHolder : RecyclerView.ViewHolder {

        @Suppress("ConvertSecondaryConstructorToPrimary")
        constructor(context: Context): super(Space(context).apply {
            layoutParams = ViewGroup.LayoutParams(dip(12), matchParent)
        })
    }

    inner class ItemHolder(private val binding: ItemThumbBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bindView(position: Int) {
            with(binding.root.context) {
                binding.ivThumb.load(File(rootDir, "$dirname/$position.mp4")) {
                    videoFrameMillis(0)
                    error(resources.getIdentifier("ic_$position", "drawable", packageName))
                }
            }
        }
    }
}