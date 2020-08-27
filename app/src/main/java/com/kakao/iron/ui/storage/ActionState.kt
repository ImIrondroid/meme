package com.kakao.iron.ui.storage

import java.io.Serializable

sealed class ActionState: Serializable {
    object Normal: ActionState()
    object Add: ActionState()
    object Delete: ActionState()
}