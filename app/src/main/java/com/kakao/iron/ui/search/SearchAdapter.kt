package com.kakao.iron.ui.search

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
import com.kakao.iron.util.OnItemSelectedListener
import com.squareup.picasso.Picasso

class SearchAdapter : PagingDataAdapter<SearchData, SearchAdapter.ViewHolder>(
    object: DiffUtil.ItemCallback<SearchData>() {
        override fun areItemsTheSame(oldItem: SearchData, newItem: SearchData): Boolean {
            return oldItem.imageUrl == newItem.imageUrl
        }
        override fun areContentsTheSame(oldItem: SearchData, newItem: SearchData): Boolean {
            return oldItem.collection == newItem.collection &&
                    oldItem.thumbnailUrl == newItem.thumbnailUrl &&
                    oldItem.documentUrl == newItem.documentUrl &&
                    oldItem.imageWidth == newItem.imageWidth &&
                    oldItem.imageHeight == newItem.imageHeight
        }
    }
) {

    private var onItemSelectedListener : OnItemSelectedListener<SearchData>? = null

    fun setOnItemSelectedListener(onItemSelectedListener: OnItemSelectedListener<SearchData>) {
        this.onItemSelectedListener = onItemSelectedListener
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        val thumbnailUrl = item?.thumbnailUrl
        val width = item?.imageWidth
        val height = item?.imageHeight
        val set = ConstraintSet()
        val ratio = String.format("%d: %d", width, height)
        Picasso
            .get()
            .load(thumbnailUrl)
            .into(holder.mImageView)
        set.apply {
            clone(holder.mConstraintLayout)
            setDimensionRatio(holder.mCard.id, ratio)
            applyTo(holder.mConstraintLayout)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view: View = layoutInflater.inflate(R.layout.item_image, parent, false)
        return ViewHolder(view)
    }

    inner class ViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {
        val mConstraintLayout: ConstraintLayout = itemView.findViewById(R.id._item_const)
        val mImageView: ImageView = itemView.findViewById(R.id.item_image)
        val mCard: CardView = itemView.findViewById(R.id._card)
        init {
            itemView.setOnClickListener {
                onItemSelectedListener?.invoke(it ,getItem(absoluteAdapterPosition)?: SearchData(), absoluteAdapterPosition)
            }
        }
    }
}