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

    val items = mutableListOf(0, 1, 2, 3, 4, 0)

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
        val item = items[position]
        if (holder is ItemHolder) {
            holder.bindView(item)
        }
    }

    override fun getItemViewType(position: Int) = items[position]

    override fun getItemCount() = items.size

    class SpaceHolder : RecyclerView.ViewHolder {

        @Suppress("ConvertSecondaryConstructorToPrimary")
        constructor(context: Context): super(Space(context).apply {
            layoutParams = ViewGroup.LayoutParams(dip(16), 0)
        })
    }

    class ItemHolder(private val binding: ItemThumbBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bindView(item: Int) {
            with(binding.root.context) {
                binding.ivThumb.load(item.toString()) {
                    error(resources.getIdentifier("ic_$item", "drawable", packageName))
                }
            }
        }
    }
}