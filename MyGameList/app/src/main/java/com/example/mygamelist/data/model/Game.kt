package com.example.mygamelist.data.model

data class Game(
    val id: Int,
    val title: String,
    val genre: String,
    val year: String,
    val imageRes: Int,
    val imageUrl: String?,
    val rating: Int,
    val status: GameStatus
)

enum class GameStatus {
    ALL, COMPLETED, PLAYING, DROPPED, WANT
}