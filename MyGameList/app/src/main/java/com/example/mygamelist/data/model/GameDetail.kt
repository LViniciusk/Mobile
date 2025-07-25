package com.example.mygamelist.data.model

data class GameDetail(
    val id: Int,
    val title: String,
    val description: String,
    val imageUrl: String?,
    val releaseYear: String?,
    val metacriticRating: Int?,
    val genres: String?,
    val rating: Double,
    val platforms: String?,
    val websiteUrl: String?,
    val publishers: String?,
    val developers: String?,
    val tags: String?,
    val screenshots: List<String>
)

fun GameDetail.toGameEntity(
    status: GameStatus = GameStatus.NONE,
    userRating: Int? = null,
    userReview: String? = null
): Game {
    return Game(
        id = this.id,
        title = this.title,
        imageUrl = this.imageUrl,
        releaseYear = this.releaseYear,
        metacriticRating = this.metacriticRating,
        genres = this.genres,
        status = status,
        userRating = userRating,
        userReview = userReview,
        cacheType = GameCacheType.NONE,
        lastUpdated = null
    )
}