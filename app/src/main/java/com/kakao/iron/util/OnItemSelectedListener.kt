package com.kakao.iron.util

import android.view.View

typealias OnItemSelectedListener<T> = ((view: View, item : T, position: Int) -> Unit)