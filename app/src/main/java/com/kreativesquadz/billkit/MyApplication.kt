package com.kreativesquadz.billkit

import soup.neumorphism.BuildConfig
import timber.log.Timber
import javax.inject.Inject
import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.kreativesquadz.billkit.utils.PreferencesHelper
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class MyApplication : Application(), Configuration.Provider {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    @Inject
    lateinit var preferencesHelper: PreferencesHelper

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }
    override fun onCreate() {
        super.onCreate()
        preferencesHelper.removeSelectedDate()
        preferencesHelper.removeSelectedDateCreditNote()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }


}