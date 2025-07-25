package com.example.mygamelist.data.model


data class GameDetailResponse(
    val id: Int,
    val slug: String,
    val name: String,
    val description_raw: String?,
    val metacritic: Int?,
    val released: String?,
    val background_image: String?,
    val website: String?,
    val rating: Double,
    val rating_top: Int,
    val platforms: List<PlatformWrapper>?,
    val genres: List<Genre>?,
    val publishers: List<Publisher>?,
    val developers: List<Developer>?,
    val tags: List<Tag>?,
    val stores: List<StoreWrapper>?,
    val short_screenshots: List<Screenshot>?
)

data class Publisher(
    val id: Int,
    val name: String,
    val slug: String
)

data class Developer(
    val id: Int,
    val name: String,
    val slug: String
)

data class Tag(
    val id: Int,
    val name: String,
    val slug: String,
    val language: String,
    val games_count: Int,
    val image_background: String?
)

data class Screenshot(
    val id: Int,
    val image: String
)

data class PlatformWrapper(
    val platform: Platform,
    val released_at: String?,
    val requirements_en: Requirements?,
    val requirements_ru: Requirements?
)


data class Platform(
    val id: Int,
    val name: String,
    val slug: String,
    val image: String?,
    val year_end: Int?,
    val year_start: Int?,
    val games_count: Int,
    val image_background: String?
)

data class Requirements(
    val minimum: String?,
    val recommended: String?
)

data class StoreWrapper(
    val id: Int,
    val store: Store
)


data class Store(
    val id: Int,
    val name: String,
    val slug: String,
    val domain: String,
    val games_count: Int,
    val image_background: String?
)

