package com.kakao.iron.ui.camera

sealed class FileState {
    object Normal: FileState()
    object Delete: FileState()
}