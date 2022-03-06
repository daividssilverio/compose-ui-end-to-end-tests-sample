package com.example.testzeuitests

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.InstallIn
import dagger.hilt.android.EarlyEntryPoint
import dagger.hilt.android.EarlyEntryPoints
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.components.SingletonComponent

@HiltAndroidApp
class DummyApplication : SuchApplication()

open class SuchApplication : Application(), Configuration.Provider {
    @EarlyEntryPoint
    @InstallIn(SingletonComponent::class)
    internal interface ApplicationEarlyEntryPoint {
        fun workerFactory(): HiltWorkerFactory
    }

    override fun getWorkManagerConfiguration(): Configuration {
        val workerFactory = EarlyEntryPoints.get(this, ApplicationEarlyEntryPoint::class.java).workerFactory()
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }
}