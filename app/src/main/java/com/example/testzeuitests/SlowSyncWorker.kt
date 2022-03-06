package com.example.testzeuitests

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.android.EarlyEntryPoints
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.UUID

@HiltWorker
class SlowSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted parameters: WorkerParameters,
    private val appDao: DummyDao
) : CoroutineWorker(context, parameters) {

    override suspend fun doWork(): Result {
        try {
            withContext(Dispatchers.IO) {
                delay(5000L)
                val user = appDao.getUser()

                val data =
                    (1..100).map { SomeData(value = UUID.randomUUID().toString() + user.name) }

                appDao.addData(data)
            }
        } catch (any: Exception) {
            return Result.failure()
        }
        return Result.success()
    }

    companion object {
        const val name = "slow-worker"
    }
}