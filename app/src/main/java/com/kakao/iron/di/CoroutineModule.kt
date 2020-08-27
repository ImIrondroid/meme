package com.kakao.iron.di

import com.kakao.iron.util.coroutine.Dispatchers
import com.kakao.iron.util.coroutine.DispatchersImpl
import org.koin.dsl.module

val coroutineModule = module {

    single<Dispatchers> {
        DispatchersImpl()
    }
}