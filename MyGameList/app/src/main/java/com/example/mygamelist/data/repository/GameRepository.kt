package com.example.mygamelist.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.mygamelist.data.api.RawgService
import com.example.mygamelist.data.model.Game
import com.example.mygamelist.data.model.toDomainGame
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter


interface GameRepository {
    fun getNewReleaseGames(): Flow<List<Game>>
    fun getComingSoonGames(): Flow<List<Game>>
}

class GameRepositoryImpl(
    private val rawgService: RawgService,
    private val apiKey: String
) : GameRepository {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun getNewReleaseGames(): Flow<List<Game>> = flow {
        val today = LocalDate.now()
        val oneMonthAgo = today.minusMonths(1)
        val dateRange = "${oneMonthAgo.format(DateTimeFormatter.ISO_DATE)},${today.format(DateTimeFormatter.ISO_DATE)}"

        try {
            val response = rawgService.getNewReleases(apiKey, dateRange)
            val games = response.results.map { it.toDomainGame() }
            emit(games)
        } catch (e: Exception) {
            emit(emptyList())
            e.printStackTrace()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun getComingSoonGames(): Flow<List<Game>> = flow {
        val today = LocalDate.now()
        val threeMonthsFromNow = today.plusMonths(3)
        val dateRange = "${today.format(DateTimeFormatter.ISO_DATE)},${threeMonthsFromNow.format(DateTimeFormatter.ISO_DATE)}"

        try {
            val response = rawgService.getComingSoon(apiKey, dateRange)
            val games = response.results.map { it.toDomainGame() }
            emit(games)
        } catch (e: Exception) {
            emit(emptyList())
            e.printStackTrace()
        }
    }
}