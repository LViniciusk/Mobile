package com.example.mygamelist.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp

data class Notification(
    @DocumentId val id: String = "",
    val recipientId: String = "",
    val actorId: String = "",
    val actorName: String = "",
    val actorProfileImageUrl: String? = null,
    val type: String = "",
    @ServerTimestamp val timestamp: Timestamp? = null,
    @get:PropertyName("isRead")
    val isRead: Boolean = false
)