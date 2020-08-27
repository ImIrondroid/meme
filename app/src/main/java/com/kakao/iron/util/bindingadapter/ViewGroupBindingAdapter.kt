package com.kakao.iron.util.bindingadapter

import android.view.View
import android.view.ViewGroup
import androidx.databinding.BindingAdapter
import com.kakao.iron.ui.storage.ActionState

@BindingAdapter("groupState")
fun ViewGroup.setAction(
    state: ActionState?
) {
    if(state!=null) {
        when(state) {
            ActionState.Normal -> {
                this.visibility = View.VISIBLE
            }
            ActionState.Add -> {
                this.visibility = View.GONE
            }
            ActionState.Delete -> {
                this.visibility = View.VISIBLE
            }
        }
    }
}