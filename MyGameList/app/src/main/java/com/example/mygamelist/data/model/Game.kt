package com.example.mygamelist.data.model

import androidx.room.TypeConverter
import androidx.room.Entity
import androidx.room.ProvidedTypeConverter
import javax.inject.Inject

@Entity(tableName = "games", primaryKeys = ["id", "cacheType", "userId"])
data class Game(
    val id: Int = 0,
    val title: String = "",
    val imageUrl: String? = null,
    val releaseYear: String? = null,
    val metacriticRating: Int? = 0,
    val genres: String? = null,
    val status: GameStatus = GameStatus.NONE,
    val userRating: Int? = null,
    val userReview: String? = null,
    val cacheType: GameCacheType = GameCacheType.NONE,
    val lastUpdated: Long? = null,
    val userId: String = ""
)


enum class GameStatus {
    NONE, CONCLUIDO, JOGANDO, ABANDONADO, DESEJO
}

enum class GameCacheType {
    NONE,
    NEW_RELEASE,
    COMING_SOON
}


@ProvidedTypeConverter
class GameCacheTypeConverter @Inject constructor() {
    @TypeConverter
    fun fromGameCacheType(type: GameCacheType?): String? {
        return type?.name
    }

    @TypeConverter
    fun toGameCacheType(typeName: String?): GameCacheType? {
        return typeName?.let { GameCacheType.valueOf(it) }
    }
}

@ProvidedTypeConverter
class GameStatusConverter @Inject constructor() {

    @TypeConverter
    fun fromGameStatus(status: GameStatus?): String? {
        return status?.name
    }

    @TypeConverter
    fun toGameStatus(statusName: String?): GameStatus? {
        return statusName?.let { GameStatus.valueOf(it) }
    }
}