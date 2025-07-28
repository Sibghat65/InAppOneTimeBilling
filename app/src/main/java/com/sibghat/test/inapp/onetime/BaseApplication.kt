package com.sibghat.test.inapp.onetime

import android.app.Application
import com.sibghat.test.inapp.onetime.di.appModule
import com.sibghat.test.inapp.onetime.di.useCaseModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class BaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@BaseApplication)
            modules(listOf(appModule, useCaseModule))
        }
    }
}