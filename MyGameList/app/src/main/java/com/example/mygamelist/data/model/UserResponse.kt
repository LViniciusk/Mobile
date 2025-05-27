package com.example.mygamelist.data.model

data class UserResponse(
    val results: List<GameResult>
)

data class UserResult(
    val displayName: String,
    val username: String,
    val gamesCount: Int,
    val followersCount: Int,
    val avatarUrl: String? = null
)

