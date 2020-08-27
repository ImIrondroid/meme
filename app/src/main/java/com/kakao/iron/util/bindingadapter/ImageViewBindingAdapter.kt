package com.kakao.iron.util.bindingadapter

import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.kakao.iron.R
import com.kakao.iron.ui.camera.FileState
import com.kakao.iron.ui.storage.ActionState

@BindingAdapter("loadFile")
fun ImageView.loadFile(
    path : String?
) {
    if(path.isNullOrEmpty()) return
    Glide
        .with(this.context)
        .load(path)
        .centerCrop()
        .error(R.drawable.ic_launcher_foreground)
        .into(this)
    /*Picasso
        .get()
        .load(File(path))
        .error(R.drawable.ic_launcher_foreground)
        .into(this)
    post {
        //TODO : rotate 과정 시간 측정
        val bitmap: Bitmap = decodeSampledBitmap(path, width, height)
        setImageBitmap(bitmap)
    }*/
}

@BindingAdapter("imageState")
fun ImageView.setVisibility(
    state: ActionState?
) {
    if(state!=null) {
        when(state) {
            ActionState.Normal -> {
                this.visibility = View.INVISIBLE
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

@BindingAdapter("fileState")
fun ImageView.setFileState(
    state: FileState?
) {
    if(state!=null) {
        when(state) {
            FileState.Normal -> {
                this.visibility = View.INVISIBLE
            }
            FileState.Delete -> {
                this.visibility = View.VISIBLE
            }
        }
    }
}