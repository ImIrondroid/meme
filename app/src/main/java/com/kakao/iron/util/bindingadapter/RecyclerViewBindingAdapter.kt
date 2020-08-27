 package com.kakao.iron.util.bindingadapter

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kakao.iron.ui.base.BaseRecyclerViewAdapter
import com.kakao.iron.util.SpacesItemDecoration

 @BindingAdapter("adapter")
fun RecyclerView.binding(adapter: RecyclerView.Adapter<*>? = null) {
    this.adapter = adapter
}

@Suppress("UNCHECKED_CAST")
@BindingAdapter("submitList")
fun<T> RecyclerView.binding(list: List<T>? = null) {
    (adapter as? BaseRecyclerViewAdapter<T>)?.run {
        submitList(list)
    }
}

@BindingAdapter("itemSpacing")
fun RecyclerView.binding(itemSpacing : Int) {
    val view = this
    if(view.itemDecorationCount == 0) {
        view.addItemDecoration(SpacesItemDecoration(itemSpacing))
    }
}