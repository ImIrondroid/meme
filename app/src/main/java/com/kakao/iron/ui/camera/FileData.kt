package com.kakao.iron.ui.camera

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.kakao.iron.BR

data class FileData(
    val filePath: String = "",
    private var _fileState: FileState = FileState.Normal
): BaseObservable() {

    @get:Bindable var fileState
        get() = _fileState
        set(value) {
            _fileState = value
            notifyPropertyChanged(BR.fileState)
        }
}