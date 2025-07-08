package com.example.cruditemapp.model

import com.google.firebase.firestore.DocumentId


data class Item(
    @DocumentId
    val id: String = "",
    val title: String = "",
    val description: String = ""
)