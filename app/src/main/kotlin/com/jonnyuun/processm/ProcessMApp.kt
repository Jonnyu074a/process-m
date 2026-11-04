package com.jonnyuun.processm

import android.app.Application
import com.jonnyuun.processm.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ProcessMApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@ProcessMApp)
            modules(appModule)
        }
    }
}