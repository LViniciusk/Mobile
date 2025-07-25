package com.example.mygamelist.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.mygamelist.data.api.ImgurService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import javax.inject.Inject
import javax.inject.Named

interface ImgurRepository {
    suspend fun uploadImage(imageUri: Uri): String
}

class ImgurRepositoryImpl @Inject constructor(
    private val imgurService: ImgurService,
    @Named("imgur_client_id") private val imgurClientId: String,
    @ApplicationContext private val context: Context
) : ImgurRepository {

    override suspend fun uploadImage(imageUri: Uri): String {
        return withContext(Dispatchers.IO) {
            Log.d("ImgurRepository", "Iniciando upload para Imgur de URI: $imageUri")
            try {
                val inputStream = context.contentResolver.openInputStream(imageUri)
                val imageBytes = inputStream?.readBytes() ?: throw IOException("Não foi possível ler a imagem da URI.")
                inputStream.close()

                val requestBody = imageBytes.toRequestBody("image/*".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("image", "profile_image.jpg", requestBody)

                val authorizationHeader = "Client-ID $imgurClientId"
                val response = imgurService.uploadImage(authorizationHeader, imagePart)

                if (response.success) {
                    Log.d("ImgurRepository", "Upload Imgur bem-sucedido. Link: ${response.data?.link}")
                    response.data?.link ?: throw IOException("URL da imagem Imgur não encontrada na resposta.")
                } else {
                    val errorMessage = response.data?.let { "Erro Imgur: ${it.description}" } ?: "Erro desconhecido no upload Imgur."
                    Log.e("ImgurRepository", "Falha no upload Imgur: $errorMessage Status: ${response.status}")
                    throw IOException(errorMessage)
                }
            } catch (e: Exception) {
                Log.e("ImgurRepository", "Exceção durante o upload Imgur: ${e.localizedMessage}", e)
                throw e
            }
        }
    }
}