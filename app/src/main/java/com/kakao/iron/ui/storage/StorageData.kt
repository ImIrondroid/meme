package com.kakao.iron.ui.storage

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.kakao.iron.BR
import java.io.Serializable

data class StorageData(
    val id : Long = 0L,
    val form: Int = 0,
    val query: String = "",
    val filePath : String = "default",
    val collection : String = "",
    val thumbnailUrl: String = "",
    val imageUrl: String = "",
    val imageWidth: Int = 0,
    val imageHeight: Int = 0,
    val displaySiteName: String = "",
    val documentUrl: String = "",
    val date: String = "",
    val text: List<String> = listOf(),
    val label: List<String> = listOf(),
    private var _action: ActionState = ActionState.Normal,
    private var _special: Boolean = false
): BaseObservable(), Serializable {

    /*
    @Bindable
    fun getAction() = action
    fun setAction(action: ActionState) {
        this.action = action
        notifyPropertyChanged(BR.action)
    }
    */

    @get:Bindable var action
        get() = _action
        set(value) {
            _action = value
            notifyPropertyChanged(BR.action)
        }

    @get:Bindable var special
        get() = _special
        set(value) {
            _special = value
            notifyPropertyChanged(BR.special)
        }
}