package com.kakao.iron.ui.special

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.kakao.iron.R
import com.kakao.iron.ui.storage.StorageData
import com.kakao.iron.util.OnItemSelectedListener
import com.squareup.picasso.Picasso
import java.io.File

class SpecialAdapter : PagingDataAdapter<StorageData, RecyclerView.ViewHolder>(
    object: DiffUtil.ItemCallback<StorageData>() {
        override fun areItemsTheSame(oldItem: StorageData, newItem: StorageData): Boolean {
            return oldItem.id == newItem.id
        }
        override fun areContentsTheSame(oldItem: StorageData, newItem: StorageData): Boolean {
            return oldItem.filePath == newItem.filePath
        }
    }
) {
    private var onItemSelectedListener : OnItemSelectedListener<StorageData>? = null

    fun setOnItemSelectedListener(onItemSelectedListener: OnItemSelectedListener<StorageData>) {
        this.onItemSelectedListener = onItemSelectedListener
    }

    override fun getItemViewType(position: Int): Int = getItem(position)?.form ?: 0

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position) ?: StorageData()
        val set = ConstraintSet()
        val ratio = String.format("%d: %d", item.imageWidth, item.imageHeight)
        when(item.form) {
            0 -> {
                Picasso
                    .get()
                    .load(item.thumbnailUrl)
                    .error(R.drawable.ic_launcher_foreground)
                    .into((holder as ItemSpecialViewHolder).mImageView)
                set.let {
                    it.clone(holder.mConstraintLayout)
                    it.setDimensionRatio(holder.mCard.id, ratio)
                    it.setDimensionRatio(holder.mImageView.id, ratio)
                    it.applyTo(holder.mConstraintLayout)
                }
            }
            else -> {
                val file = File(item.filePath)
                when(file.exists()) {
                    true -> {
                        Picasso
                            .get()
                            .load(file)
                            .error(R.drawable.ic_launcher_foreground)
                            .fit()
                            .into((holder as ItemSpecialFileViewHolder).mImageView)
                    }
                    false -> {
                        Picasso
                            .get()
                            .load(R.drawable.ic_launcher_foreground)
                            .into((holder as ItemSpecialFileViewHolder).mImageView)
                    }
                }
            }
        }
        holder.itemView.setOnClickListener {
            onItemSelectedListener?.invoke(it, item, position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val normalView: View = layoutInflater.inflate(R.layout.item_special, parent, false)
        val fileView: View = layoutInflater.inflate(R.layout.item_special_file, parent, false)
        return when(viewType) {
            0 -> ItemSpecialViewHolder(normalView)
            else -> ItemSpecialFileViewHolder(fileView)
        }
    }

    inner class ItemSpecialViewHolder(
        itemView: View
    ): RecyclerView.ViewHolder(itemView) {
        val mConstraintLayout: ConstraintLayout = itemView.findViewById(R.id.constraint)
        val mImageView: ImageView = itemView.findViewById(R.id._image)
        val mCard: CardView = itemView.findViewById(R.id._card)
    }

    inner class ItemSpecialFileViewHolder(
        itemView: View
    ): RecyclerView.ViewHolder(itemView) {
        val mImageView: ImageView = itemView.findViewById(R.id._image)
    }
}