package com.kakao.iron.di

import android.content.Context
import com.facebook.flipper.plugins.network.FlipperOkhttpInterceptor
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.kakao.iron.data.remote.SearchApi
import com.kakao.iron.ui.MemeApplication.Companion.networkFlipperPlugin
import com.kakao.iron.util.HttpInterceptor
import okhttp3.OkHttpClient
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val networkModule = module {

    single {
        get<Retrofit>().create(SearchApi::class.java)
    }

    single {
        Retrofit.Builder()
            .baseUrl("https://dapi.kakao.com")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .client(get())
            .build()
    }

    single {
        OkHttpClient.Builder()
            .addNetworkInterceptor(FlipperOkhttpInterceptor(networkFlipperPlugin))
            .addInterceptor(HttpInterceptor(get() as Context))
            .build()
    }
}