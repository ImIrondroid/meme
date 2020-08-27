package com.kakao.iron.di

import com.kakao.iron.data.local.preferences.PreferenceHelper
import com.kakao.iron.data.local.room.db.MemeDatabase
import org.koin.dsl.module

val databaseModule = module {

    single {
        PreferenceHelper(get())
    }

    single {
        MemeDatabase.getInstance(get())
    }
}