package com.example.mygamelist.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.mygamelist.data.repository.GameRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit


@HiltWorker
class CacheUpdateWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val gameRepository: GameRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d(TAG, "Iniciando CacheUpdateWorker para atualizar caches de jogos.")
        try {
            gameRepository.refreshNewReleaseGamesCache()
            gameRepository.refreshComingSoonGamesCache()

            Log.d(TAG, "Caches de jogos atualizados com sucesso.")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Falha ao atualizar caches de jogos: ${e.localizedMessage}", e)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "CacheUpdateWorker"

        const val WORK_NAME = "MyGameListCacheUpdateWorker"

        fun createPeriodicWorkRequest(repeatInterval: Long): androidx.work.PeriodicWorkRequest {
            return androidx.work.PeriodicWorkRequestBuilder<CacheUpdateWorker>(
                repeatInterval, TimeUnit.HOURS
            )
                .setInitialDelay(10, TimeUnit.MINUTES)
                .addTag(WORK_NAME)
                .build()
        }
    }
}