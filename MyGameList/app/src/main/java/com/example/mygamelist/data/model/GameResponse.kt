package com.example.mygamelist.data.model

import android.os.Build
import androidx.annotation.RequiresApi

data class GameResponse(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<GameResult>
)

data class GameResult(
    val id: Int,
    val name: String,
    val released: String?,
    val background_image: String?,
    val metacritic: Int?,
    val updated: String,
    val genres: List<Genre>?,
)

data class Genre(
    val id: Int,
    val name: String,
)



@RequiresApi(Build.VERSION_CODES.O)
fun GameResult.toDomainGame(): Game {

    val genresString = this.genres?.joinToString(", ") { it.name }

    return Game(
        id = this.id,
        title = this.name,
        imageUrl = this.background_image,
        releaseYear = this.released,
        metacriticRating = this.metacritic,
        genres = genresString,
        status = GameStatus.NONE
    )
}
