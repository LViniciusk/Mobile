package com.example.mygamelist.data.api

import com.example.mygamelist.data.model.GameDetailResponse
import com.example.mygamelist.data.model.GameResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query


interface RawgService {

    @GET("games")
    suspend fun searchGames(
        @Query("key") apiKey: String,
        @Query("search") query: String
    ): GameResponse


    @GET("games")
    suspend fun getNewReleases(
        @Query("key") apiKey: String,
        @Query("dates") dateRange: String,
        @Query("ordering") ordering: String = "-released"
    ): GameResponse


    @GET("games")
    suspend fun getComingSoon(
        @Query("key") apiKey: String,
        @Query("dates") dateRange: String,
        @Query("ordering") ordering: String = "released"
    ): GameResponse

    @GET("games/{id}")
    suspend fun getGameDetails(
        @Path("id") gameId: Int,
        @Query("key") apiKey: String
    ): GameDetailResponse
}
//84f487f7f7ce4190a0911d867db3c1ef
