package com.automate123.videshorts.screen.main

import android.content.Context
import android.graphics.Color
import android.view.ViewGroup
import android.widget.Space
import androidx.recyclerview.widget.RecyclerView
import coil.dispose
import coil.load
import com.automate123.videshorts.databinding.ItemThumbBinding
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerColorDrawable
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

    var mDirname: String? = null

    var mPosition = 0

    private var iconsCount = 0

    private val itemsCount: Int
        get() = max(mPosition, iconsCount)

    init {
        with(context) {
            while (true) {
                val position = iconsCount + 1
                val id = resources.getIdentifier("ic_$position", "drawable", packageName)
                if (id == 0) {
                    break
                }
                iconsCount = position
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
            holder.bind(position)
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        if (holder is ItemHolder) {
            holder.unbind()
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (position > itemsCount) {
            return 0
        }
        return position
    }

    override fun getItemCount() = 1 + itemsCount + 1

    class SpaceHolder : RecyclerView.ViewHolder {

        @Suppress("ConvertSecondaryConstructorToPrimary")
        constructor(context: Context): super(Space(context).apply {
            layoutParams = ViewGroup.LayoutParams(dip(12), matchParent)
        })
    }

    inner class ItemHolder(private val binding: ItemThumbBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) {
            with(binding.root.context) {
                val iconId = resources.getIdentifier("ic_$position", "drawable", packageName)
                if (position <= mPosition) {
                    binding.ivThumb.load(File(rootDir, "$mDirname/$position.mp4")) {
                        placeholder(ShimmerColorDrawable(Color.WHITE).apply {
                            setShimmer(Shimmer.ColorHighlightBuilder().build())
                        })
                        error(iconId)
                    }
                } else {
                    binding.ivThumb.load(iconId) {
                        error(null)
                    }
                }
            }
        }

        fun unbind() {
            binding.ivThumb.dispose()
        }
    }
}