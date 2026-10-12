package com.example.finderskeepersapplication.worker

import android.content.Context
import androidx.work.*
import com.example.finderskeepersapplication.data.local.AppDatabase
import com.example.finderskeepersapplication.data.remote.RetrofitClient
import com.example.finderskeepersapplication.data.repository.FindersKeepersRepository
import java.util.concurrent.TimeUnit

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val itemDao = AppDatabase.getDatabase(applicationContext).itemDao()
            val repository = FindersKeepersRepository(
                itemDao,
                RetrofitClient.remotePostsApi,
                applicationContext
            )
            repository.syncPendingItems()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        fun schedulePeriodicSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncWorkRequest = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "FindersKeepersSyncWork",
                ExistingPeriodicWorkPolicy.KEEP,
                syncWorkRequest
            )
        }

        fun triggerOneTimeSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncWorkRequest = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueue(syncWorkRequest)
        }
    }
}
