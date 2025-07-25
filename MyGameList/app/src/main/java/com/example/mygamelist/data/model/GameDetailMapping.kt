package com.example.mygamelist.data.model

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import kotlin.collections.joinToString

@RequiresApi(Build.VERSION_CODES.O)
fun GameDetailResponse.toDomainGameDetail(): GameDetail {
    val releaseYear = try {
        this.released?.let {
            LocalDate.parse(it, DateTimeFormatter.ISO_DATE).year.toString()
        }
    } catch (e: DateTimeParseException) {
        null
    }

    val genresString = this.genres?.joinToString(", ") { it.name }
    val platformsString = this.platforms?.joinToString(", ") { it.platform.name }
    val publishersString = this.publishers?.joinToString(", ") { it.name }
    val developersString = this.developers?.joinToString(", ") { it.name }
    val tagsString = this.tags?.joinToString(", ") { it.name }
    val screenshotUrls = this.short_screenshots?.map { it.image } ?: emptyList()

    return GameDetail(
        id = this.id,
        title = this.name,
        description = this.description_raw ?: "Nenhuma descrição disponível.",
        imageUrl = this.background_image,
        releaseYear = releaseYear,
        metacriticRating = this.metacritic,
        genres = genresString,
        platforms = platformsString,
        websiteUrl = this.website,
        publishers = publishersString,
        developers = developersString,
        tags = tagsString,
        screenshots = screenshotUrls,
        rating = this.rating
    )
}