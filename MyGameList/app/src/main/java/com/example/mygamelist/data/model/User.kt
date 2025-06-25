package com.example.mygamelist.data.model

data class User(
    val id: Int,
    val name: String,
    val username: String,
    val quote: String,
    val stats: UserStats,
    val avatarUrl: String?,
    val userGames: List<Game>,
    val followersCount: Int,
    val followingCount: Int
)

data class UserStats(
    val todos: Int = 0,
    val concluidos: Int = 0,
    val jogando: Int = 0,
    val abandonados: Int = 0,
    val desejados: Int = 0
)


val UsersList = listOf(
    UserResult(
        displayName = "Korleon",
        username = "Korleon",
        gamesCount = 0,
        followersCount = 0
    ),
    UserResult(
        displayName = "Nodd",
        username = "Nodd",
        gamesCount = 2,
        followersCount = 0
    ),
    UserResult(
        displayName = "Bandr Bh",
        username = "BandrBh",
        gamesCount = 6,
        followersCount = 0
    ),
    UserResult(
        displayName = "mantas tf.com",
        username = "nub.x",
        gamesCount = 69,
        followersCount = 1
    ),
    UserResult(
        displayName = "LeLeGoGo",
        username = "LeLeGoGo",
        gamesCount = 109,
        followersCount = 0
    ),
    UserResult(
        displayName = "TeddyBear05",
        username = "TeddyBear05",
        gamesCount = 100,
        followersCount = 1
    )
)