package com.example.mygamelist.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.mygamelist.data.dao.GameDao
import com.example.mygamelist.data.model.Game
import com.example.mygamelist.data.model.GameCacheTypeConverter
import com.example.mygamelist.data.model.GameStatusConverter


@Database(entities = [Game::class], version = 7, exportSchema = false)
@TypeConverters(GameStatusConverter::class, GameCacheTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao
}