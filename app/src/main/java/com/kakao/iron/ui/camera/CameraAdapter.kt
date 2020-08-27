package com.kakao.iron.ui.camera

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.load.DecodeFormat
import com.kakao.iron.R
import com.kakao.iron.databinding.ItemCameraBinding
import com.kakao.iron.ui.base.BaseRecyclerViewAdapter
import com.kakao.iron.ui.base.BaseViewHolder
import com.kakao.iron.util.OnItemSelectedListener
import com.kakao.iron.util.decodeSampledBitmap
import com.squareup.picasso.Picasso
import java.io.File

class CameraAdapter : BaseRecyclerViewAdapter<FileData>(
    object : DiffUtil.ItemCallback<FileData>() {
        override fun areItemsTheSame(oldItem: FileData, newItem: FileData): Boolean {
            return oldItem.filePath == newItem.filePath
        }
        override fun areContentsTheSame(oldItem: FileData, newItem: FileData): Boolean {
            return oldItem.fileState == newItem.fileState
        }
    }
) {

    private var onItemSelectedListener: OnItemSelectedListener<FileData>? = null

    fun setOnItemSelectedListener(onItemSelectedListener: OnItemSelectedListener<FileData>) {
        this.onItemSelectedListener = onItemSelectedListener
    }

    override fun onBindViewHolder(holder: BaseViewHolder<FileData>, position: Int) {
        super.onBindViewHolder(holder, position)
        val mHolder = (holder as ItemCameraViewHolder)
        val item = getItem(position) ?: FileData()
        val file = File(item.filePath)
        when(file.exists()) {
            true -> {
                /*holder.image.setImageBitmap(BitmapFactory.decodeFile(item.filePath))
                val target = holder.image
                target.post {
                    val bitmap: Bitmap = decodeSampledBitmap(item.filePath, target.width, target.height)
                    target.setImageBitmap(bitmap)
                }*/
                Picasso
                    .get()
                    .load(File(item.filePath))
                    .resize(160,184)
                    .into(mHolder.image)
            }
            false -> {
                Picasso
                    .get()
                    .load(R.drawable.ic_launcher_foreground)
                    .into(mHolder.image)
            }
        }
        mHolder.itemView.setOnClickListener {
            onItemSelectedListener?.invoke(holder.itemView, item, holder.absoluteAdapterPosition)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder<FileData> {
        return ItemCameraViewHolder(
            ItemCameraBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    inner class ItemCameraViewHolder(
        val binding: ItemCameraBinding
    ) : BaseViewHolder<FileData>(binding) {
        val delete = binding.cameraDelete
        val image = binding.cameraImage
    }
}