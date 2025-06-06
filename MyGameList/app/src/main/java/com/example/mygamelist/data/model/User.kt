package com.example.mygamelist.data.model

import com.example.mygamelist.R

data class User(
    val id: String,
    val name: String,
    val username: String,
    val quote: String,
    val stats: UserStats,
    val avatarUrl: Int,
)

data class UserStats(
    val all: Int = 0,
    val finished: Int = 0,
    val playing: Int = 0,
    val dropped: Int = 0,
    val want: Int = 0
)

val sampleUser = User(
    id = "1",
    name = "LViniciusk",
    username = "LViniciusk",
    quote = "Cold is the void",
    stats = UserStats(all = 5, finished = 2, playing = 1, dropped = 1, want = 1),
    avatarUrl = R.drawable.ic_launcher_foreground
)
val sampleGames = listOf(
    Game(
        id = 1,
        title = "Outer Wilds",
        genre = "Puzzle, Aventura",
        year = "2019",
        imageRes = R.drawable.ic_launcher_foreground,
        status = GameStatus.COMPLETED,
        rating = 10,
        imageUrl = null
    ),
    Game(
        id = 2,
        title = "Hades",
        genre = "Roguelike, Ação",
        year = "2020",
        imageRes = R.drawable.ic_launcher_foreground,
        status = GameStatus.COMPLETED,
        rating = 10,
        imageUrl = null
    )
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