package com.example.mygamelist.data.api


import okhttp3.MultipartBody
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part


interface ImgurService {
    @Multipart
    @POST("3/image")
    suspend fun uploadImage(
        @Header("Authorization") authorization: String,
        @Part image: MultipartBody.Part
    ): ImgurUploadResponse
}

data class ImgurUploadResponse(
    val data: ImgurData?,
    val success: Boolean,
    val status: Int
)

data class ImgurData(
    val id: String,
    val link: String,
    val type: String,
    val datetime: Long,
    val width: Int,
    val height: Int,
    val size: Long,
    val views: Int,
    val bandwidth: Long,
    val section: String?,
    val account_id: Long?,
    val account_url: String?,
    val deletehash: String?,
    val name: String?,
    val title: String?,
    val description: String?,
    val is_ad: Boolean,
    val is_animated: Boolean,
    val is_looping: Boolean,
    val favorite: Boolean,
    val nsfw: Boolean?,
    val vote: String?,
    val tags: List<String>?,
    val in_gallery: Boolean
)