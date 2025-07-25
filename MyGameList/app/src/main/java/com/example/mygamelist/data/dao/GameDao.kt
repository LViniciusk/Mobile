package com.example.mygamelist.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.example.mygamelist.data.model.Game
import com.example.mygamelist.data.model.GameCacheType
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {

    @Query("SELECT * FROM games WHERE cacheType = 'NONE' AND userId = :userId")
    fun getAllUserGames(userId: String): Flow<List<Game>>

    @Query("SELECT * FROM games WHERE id = :gameId AND cacheType = 'NONE' AND userId = :userId")
    suspend fun getUserGameById(gameId: Int, userId: String): Game?

    @Query("SELECT * FROM games WHERE cacheType = :type")
    fun getGamesByCacheType(type: GameCacheType): Flow<List<Game>>

    @Query("DELETE FROM games WHERE cacheType = :type")
    suspend fun deleteGamesByCacheType(type: GameCacheType)

    @Query("DELETE FROM games WHERE userId = :userId AND cacheType = 'NONE'")
    suspend fun deleteUserGames(userId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGame(game: Game)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllGames(games: List<Game>)

    @Update
    suspend fun updateGame(game: Game)

    @Delete
    suspend fun deleteGame(game: Game)

    @Query("DELETE FROM games")
    suspend fun deleteAllGames()
}