package com.kakao.iron.di

import com.kakao.iron.data.local.room.SaveRepository
import com.kakao.iron.data.local.room.SaveRepositoryImpl
import com.kakao.iron.data.remote.SearchRepository
import com.kakao.iron.data.remote.SearchRepositoryImpl
import org.koin.dsl.module

val repositoryModule = module {

    factory<SearchRepository> {
        SearchRepositoryImpl(get())
    }

    factory<SaveRepository> {
        SaveRepositoryImpl(get())
    }
}