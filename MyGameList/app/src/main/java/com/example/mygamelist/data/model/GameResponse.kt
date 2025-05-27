package com.example.mygamelist.data.model

data class GameResponse(
    val results: List<GameResult>
)

data class GameResult(
    val id: Int,
    val name: String,
    val released: String?,
    val genres: List<Genre>,
    val background_image: String?
)

data class Genre(val name: String)
