package com.kakao.iron.ui.storage

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.kakao.iron.R
import com.kakao.iron.databinding.ItemStorageBinding
import com.kakao.iron.databinding.ItemStorageFileBinding
import com.kakao.iron.ui.base.BaseRecyclerViewAdapter
import com.kakao.iron.ui.base.BaseViewHolder
import com.kakao.iron.util.OnItemSelectedListener
import com.squareup.picasso.Picasso
import java.io.File

class StorageAdapter : BaseRecyclerViewAdapter<StorageData>(
    object : DiffUtil.ItemCallback<StorageData>() {
        override fun areItemsTheSame(oldItem: StorageData, newItem: StorageData): Boolean {
            return oldItem.id == newItem.id
        }
        override fun areContentsTheSame(oldItem: StorageData, newItem: StorageData): Boolean {
            return oldItem.action == newItem.action &&
                    oldItem.thumbnailUrl == newItem.thumbnailUrl &&
                    oldItem.imageUrl == newItem.imageUrl &&
                    oldItem.filePath == newItem.filePath &&
                    oldItem.text == newItem.text &&
                    oldItem.label == newItem.label
        }
    }
) {

    private var onItemSelectedListener: OnItemSelectedListener<StorageData>? = null

    fun setOnItemSelectedListener(onItemSelectedListener: OnItemSelectedListener<StorageData>) {
        this.onItemSelectedListener = onItemSelectedListener
    }

    override fun getItemId(position: Int): Long = currentList[position].id

    override fun getItemViewType(position: Int): Int = getItem(position).form

    override fun onBindViewHolder(holder: BaseViewHolder<StorageData>, position: Int) {
        super.onBindViewHolder(holder, position)
        val item = getItem(position)
        val file = File(item.filePath)
        when(item.form) {
            0 -> {
                /*when(file.exists()) {
                    true -> {
                        Picasso
                            .get()
                            .load(file)
                            .fit()
                            .into((holder as ItemStorageViewHolder).image)
                    }
                    false -> {
                        Picasso
                            .get()
                            .load(R.drawable.ic_launcher_foreground)
                            .into((holder as ItemStorageViewHolder).image)
                    }
                }*/
                Picasso
                    .get()
                    .load(item.thumbnailUrl)
                    .into((holder as ItemStorageViewHolder).image)
            }
            1 -> {
                when(file.exists()) {
                    true -> {
                        Picasso
                            .get()
                            .load(file)
                            .fit()
                            .into((holder as ItemStorageFileViewHolder).image)
                    }
                    false -> {
                        Picasso
                            .get()
                            .load(R.drawable.ic_launcher_foreground)
                            .fit()
                            .into((holder as ItemStorageFileViewHolder).image)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder<StorageData> {
        return when(viewType) {
            0 -> ItemStorageViewHolder(
                ItemStorageBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            else -> ItemStorageFileViewHolder(
                ItemStorageFileBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    inner class ItemStorageFileViewHolder(
        val binding: ItemStorageFileBinding
    ) : BaseViewHolder<StorageData>(binding) {
        val image = binding.Image
        val delete = binding.delete
        override fun onBind(item: StorageData?) {
            super.onBind(item)
            if (item == null) return
            else {
                itemView.setOnClickListener {
                    onItemSelectedListener?.invoke(it, item, adapterPosition)
                }
            }
        }
    }

    inner class ItemStorageViewHolder(
        val binding: ItemStorageBinding
    ) : BaseViewHolder<StorageData>(binding) {
        val image = binding.Image
        val delete = binding.delete
        override fun onBind(item: StorageData?) {
            super.onBind(item)
            if (item == null) return
            else {
                itemView.setOnClickListener {
                    onItemSelectedListener?.invoke(it, item, adapterPosition)
                }
            }
        }
    }
}