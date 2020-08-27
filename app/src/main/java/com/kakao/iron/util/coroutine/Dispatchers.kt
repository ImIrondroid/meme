package com.kakao.iron.util.coroutine

import kotlinx.coroutines.CoroutineDispatcher

interface Dispatchers {

    fun main(): CoroutineDispatcher

    fun io(): CoroutineDispatcher

    fun default(): CoroutineDispatcher

    fun single(): CoroutineDispatcher

    fun unconfined(): CoroutineDispatcher
}