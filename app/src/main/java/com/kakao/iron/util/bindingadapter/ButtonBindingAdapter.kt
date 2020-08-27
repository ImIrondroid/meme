package com.kakao.iron.util.bindingadapter

import android.view.View
import android.widget.Button
import android.widget.CheckBox
import androidx.databinding.BindingAdapter
import com.kakao.iron.ui.storage.ActionState

@BindingAdapter("special")
fun CheckBox.setChecked(isSpecial: Boolean) {
    this.isChecked = isSpecial
}

@BindingAdapter("buttonState")
fun Button.setVisibility(actionState: ActionState?) {
    if(actionState!=null) {
        when(actionState) {
            ActionState.Normal -> {
                this.visibility = View.INVISIBLE
            }
            ActionState.Add -> {
                this.visibility = View.VISIBLE
            }
            ActionState.Delete -> {
                this.visibility = View.INVISIBLE
            }
        }
    }
}