package com.example.mygamelist.data.repository

import com.example.mygamelist.data.model.Notification
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

interface NotificationRepository {
    fun getNotifications(userId: String): Flow<List<Notification>>
}

class NotificationRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : NotificationRepository {

    override fun getNotifications(userId: String): Flow<List<Notification>> = callbackFlow {
        val subscription = firestore.collection("notifications")
            .whereEqualTo("recipientId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(30)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                try {
                    val notifications = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Notification::class.java)
                    }
                    trySend(notifications)
                } catch (e: Exception) {
                    close(e)
                }
            }

        awaitClose { subscription.remove() }
    }
}