package com.example.postapp.data.models
data class Post (
    val id: Int,
    val title: String,
    val content: String,
    val ownerId: Int
)
data class CreatePostRequest (
    val title: String,
    val content: String,
)