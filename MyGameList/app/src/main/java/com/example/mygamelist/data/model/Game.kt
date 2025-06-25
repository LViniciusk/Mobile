package com.example.mygamelist.data.model

data class Game(
    val id: Int,
    val title: String,
    val imageUrl: String?,
    val releaseYear: String?,
    val metacriticRating: Int?,
    val genres: String?,
    val status: GameStatus = GameStatus.NONE,
)

enum class GameStatus {
    NONE, CONCLUIDO, JOGANDO, ABANDONADO, DESEJO
}