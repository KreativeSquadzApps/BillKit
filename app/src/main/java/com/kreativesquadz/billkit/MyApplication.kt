package com.kreativesquadz.billkit

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import soup.neumorphism.BuildConfig
import timber.log.Timber


@HiltAndroidApp
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}