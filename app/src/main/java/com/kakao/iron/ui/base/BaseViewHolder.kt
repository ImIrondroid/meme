package com.kakao.iron.ui.base

import android.content.Context
import androidx.databinding.ViewDataBinding
import androidx.databinding.library.baseAdapters.BR
import androidx.recyclerview.widget.RecyclerView

open class BaseViewHolder<T>(
    private val binding: ViewDataBinding
) : RecyclerView.ViewHolder(binding.root) {

    protected val context : Context
        get() = itemView.context

    open fun onBind(item : T?) {
        binding.apply {
            setVariable(BR.item, item)
            executePendingBindings()
        }
    }
}