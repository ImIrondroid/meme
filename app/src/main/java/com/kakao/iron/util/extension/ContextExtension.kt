package com.kakao.iron.util.extension

import android.content.Context
import android.widget.Toast

fun Context.showToast(message: String): Unit =
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()


fun Context.showToast(messageRes: Int): Unit =
    Toast.makeText(this, getString(messageRes), Toast.LENGTH_SHORT).show()
