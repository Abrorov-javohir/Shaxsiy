package com.automate123.videshorts.screen.main

import android.content.Context
import android.view.ViewGroup
import android.widget.Space
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.automate123.videshorts.databinding.ItemThumbBinding
import org.jetbrains.anko.dip
import org.jetbrains.anko.layoutInflater
import javax.inject.Inject

class ThumbAdapter @Inject constructor() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var count = 4

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
            layoutParams = ViewGroup.LayoutParams(dip(16), 0)
        })
    }

    class ItemHolder(private val binding: ItemThumbBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bindView(position: Int) {
            with(binding.root.context) {
                binding.ivThumb.load(position.toString()) {
                    error(resources.getIdentifier("ic_$position", "drawable", packageName))
                }
            }
        }
    }
}