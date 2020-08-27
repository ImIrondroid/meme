package com.kakao.iron.ui.base

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter

abstract class BaseRecyclerViewAdapter<T>(
    diffCallback : DiffUtil.ItemCallback<T>
) : ListAdapter<T, BaseViewHolder<T>>(diffCallback) {

    override fun onBindViewHolder(holder: BaseViewHolder<T>, position: Int) {
        holder.onBind(currentList.getOrNull(position))
    }
}