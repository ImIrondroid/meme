package com.kakao.iron.util.coroutine

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

class DispatchersImpl : Dispatchers {

    override fun main(): CoroutineDispatcher {
        return kotlinx.coroutines.Dispatchers.Main.immediate
    }

    override fun io(): CoroutineDispatcher {
        return kotlinx.coroutines.Dispatchers.IO
    }

    override fun default(): CoroutineDispatcher {
        return kotlinx.coroutines.Dispatchers.Default
    }

    override fun single(): CoroutineDispatcher {
        return Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    }

    override fun unconfined(): CoroutineDispatcher {
        return kotlinx.coroutines.Dispatchers.Unconfined
    }
}