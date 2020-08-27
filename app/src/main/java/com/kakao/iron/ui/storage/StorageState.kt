package com.kakao.iron.ui.storage

sealed class StorageState {
    object SortAscending: StorageState()
    object SortDescending: StorageState()
    object SortQuery: StorageState()
}