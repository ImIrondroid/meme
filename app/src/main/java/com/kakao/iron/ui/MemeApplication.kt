package com.kakao.iron.ui

import android.app.Application
import android.content.Context
import com.facebook.flipper.android.AndroidFlipperClient
import com.facebook.flipper.android.utils.FlipperUtils
import com.facebook.flipper.core.FlipperClient
import com.facebook.flipper.plugins.crashreporter.CrashReporterPlugin
import com.facebook.flipper.plugins.databases.DatabasesFlipperPlugin
import com.facebook.flipper.plugins.inspector.DescriptorMapping
import com.facebook.flipper.plugins.inspector.InspectorFlipperPlugin
import com.facebook.flipper.plugins.leakcanary.LeakCanaryFlipperPlugin
import com.facebook.flipper.plugins.network.NetworkFlipperPlugin
import com.facebook.soloader.SoLoader
import com.kakao.iron.BuildConfig
import com.kakao.iron.di.*
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MemeApplication : Application() {

    private val context: Context = this

    override fun onCreate() {
        super.onCreate()
        initKoin()
        initFlipper()
    }

    private fun initKoin() {
        startKoin {
            androidContext(this@MemeApplication)
            modules(
                listOf(
                    networkModule,
                    repositoryModule,
                    databaseModule,
                    coroutineModule,
                    viewModelModule
                )
            )
        }
    }

    private fun initFlipper() {
        SoLoader.init(context, false)
        if(BuildConfig.DEBUG && FlipperUtils.shouldEnableFlipper(this)) {
            val client: FlipperClient = AndroidFlipperClient.getInstance(this).apply {
                addPlugin(InspectorFlipperPlugin(context, DescriptorMapping.withDefaults()))
                addPlugin(CrashReporterPlugin.getInstance())
                addPlugin(DatabasesFlipperPlugin(context))
                addPlugin(LeakCanaryFlipperPlugin())
                addPlugin(networkFlipperPlugin)
            }
            client.start()
        }
    }

    companion object {
        val networkFlipperPlugin = NetworkFlipperPlugin()
    }
}