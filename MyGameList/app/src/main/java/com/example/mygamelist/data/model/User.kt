package com.example.mygamelist.data.model

data class User(
    val id: String = "",
    val username: String = "",
    val name: String = "",
    val email: String = "",
    val profileImageUrl: String? = null,
    val quote: String = "",
    val followersCount: Int = 0,
    val followingCount: Int = 0
)

data class UserStats(
    val all: Int = 0,
    val finished: Int = 0,
    val playing: Int = 0,
    val dropped: Int = 0,
    val wish: Int = 0
)


