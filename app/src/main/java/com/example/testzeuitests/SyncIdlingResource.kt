package com.example.testzeuitests

import androidx.hilt.work.HiltWorkerFactory
import androidx.test.espresso.IdlingResource
import dagger.hilt.InstallIn
import dagger.hilt.android.EarlyEntryPoint
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class SyncIdlingResource @Inject constructor() : IdlingResource {
    @Volatile
    private var resourceCallback: IdlingResource.ResourceCallback? = null
    private val mIsIdleNow: AtomicBoolean = AtomicBoolean(true)

    override fun getName(): String {
        return "sync-state"
    }

    override fun isIdleNow(): Boolean {
        return mIsIdleNow.get()
    }

    override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback?) {
        resourceCallback = callback
    }

    fun setIsIdle(idle: Boolean) {
        mIsIdleNow.set(idle)
        if(isIdleNow) {
            resourceCallback?.onTransitionToIdle()
        }
    }
}

@EarlyEntryPoint
@InstallIn(SingletonComponent::class)
internal interface IdlingResources {
    fun syncIdlingResource(): SyncIdlingResource
}

