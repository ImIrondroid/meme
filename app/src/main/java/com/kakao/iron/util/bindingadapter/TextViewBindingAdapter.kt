package com.kakao.iron.util.bindingadapter

import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.kakao.iron.R
import com.kakao.iron.ui.camera.FileState
import com.kakao.iron.ui.storage.ActionState

@BindingAdapter("actionState")
fun TextView.bindingVisibility(actionState: ActionState?) {
    if(actionState!=null) {
        when(actionState) {
            ActionState.Normal -> {
                this.visibility = View.VISIBLE
            }
            ActionState.Add -> {
                this.visibility = View.INVISIBLE
            }
            ActionState.Delete -> {
                this.visibility = View.VISIBLE
            }
        }
    }
}


@BindingAdapter("textState")
fun TextView.bindingText(actionState: ActionState?) {
    val view = this as AutoCompleteTextView
    if(actionState!=null) {
        when(actionState) {
            ActionState.Normal -> {
                this.setText(view.context.getString(R.string.text_screen_default), false)
            }
            ActionState.Add -> {
                this.setText(view.context.getString(R.string.text_screen_add), false)
            }
            ActionState.Delete -> {
                this.setText(view.context.getString(R.string.text_screen_delete), false)
            }
        }
    }
}

@BindingAdapter("textFileState")
fun TextView.bindingText(fileState: FileState?) {
    val view = this as AutoCompleteTextView
    if(fileState!=null) {
        when(fileState) {
            FileState.Normal -> {
                this.setText(view.context.getString(R.string.text_screen_default), false)
            }
            FileState.Delete -> {
                this.setText(view.context.getString(R.string.text_screen_delete), false)
            }
        }
    }
}