package com.example.mygamelist

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import com.example.mygamelist.worker.CacheUpdateWorker
import android.util.Log

@HiltAndroidApp
class MainApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .build()

    override fun onCreate() {
        super.onCreate()
        schedulePeriodicCacheUpdate()
    }

    private fun schedulePeriodicCacheUpdate() {
        val periodicWorkRequest = CacheUpdateWorker.createPeriodicWorkRequest(24)

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            CacheUpdateWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            periodicWorkRequest
        )
        Log.d("MainApplication", "CacheUpdateWorker agendado com sucesso.")
    }
}