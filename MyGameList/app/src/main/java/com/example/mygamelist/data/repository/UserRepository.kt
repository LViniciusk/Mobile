package com.example.mygamelist.data.repository

import com.example.mygamelist.data.model.User
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import android.util.Log
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

interface UserRepository {
    fun getUserProfile(userId: String): Flow<User?>
    suspend fun saveUserProfile(user: User)
    suspend fun createInitialProfile(userId: String, username: String, email: String, profileImageUrl: String?)
    suspend fun getAllUsers(): List<User>
    suspend fun followUser(
        currentUserId: String,
        currentUserName: String,
        currentUserImageUrl: String?,
        targetUserId: String
    )
    suspend fun unfollowUser(currentUserId: String, targetUserId: String)
    fun isFollowing(currentUserId: String, targetUserId: String): Flow<Boolean>
    fun getFollowingIds(userId: String): Flow<List<String>>
    fun getFollowersList(userId: String): Flow<List<User>>
    fun getFollowingList(userId: String): Flow<List<User>>
}

class UserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : UserRepository {

    private val usersCollection = firestore.collection("users")


    override fun getFollowingIds(userId: String): Flow<List<String>> = callbackFlow {
        val subscription = firestore.collection("users").document(userId)
            .collection("following")
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val ids = snapshot.documents.map { it.id }
                trySend(ids)
            }
        awaitClose { subscription.remove() }
    }

    override fun getUserProfile(userId: String): Flow<User?> = callbackFlow {
        val documentRef = usersCollection.document(userId)
        val snapshotListener = documentRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.e("UserRepository", "Erro ao observar perfil do usuário: ${e.message}", e)
                trySend(null)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val user = snapshot.toObject(User::class.java)
                trySend(user)
            } else {
                trySend(null)
            }
        }
        awaitClose { snapshotListener.remove() }
    }

    override suspend fun saveUserProfile(user: User) {
        usersCollection.document(user.id).set(user).await()
    }

    override suspend fun createInitialProfile(userId: String, username: String, email: String, profileImageUrl: String?) {
        val initialProfile = User(
            id = userId,
            name = username,
            username = username,
            quote = "Olá! Este é o meu perfil no MyGameList.",
            profileImageUrl = profileImageUrl,
            followersCount = 0,
            followingCount = 0
        )
        saveUserProfile(initialProfile)
    }

    override suspend fun getAllUsers(): List<User> {
        return try {
            val snapshot = firestore.collection("users").get().await()
            snapshot.toObjects(User::class.java)
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun followUser(
        currentUserId: String,
        currentUserName: String,
        currentUserImageUrl: String?,
        targetUserId: String
    ) {
        val currentUserRef = usersCollection.document(currentUserId)
        val targetUserRef = usersCollection.document(targetUserId)

        val notificationId = "${targetUserId}_${currentUserId}"
        val notificationRef = firestore.collection("notifications").document(notificationId)

        val followData = mapOf("followedAt" to FieldValue.serverTimestamp())

        firestore.batch().apply {
            set(currentUserRef.collection("following").document(targetUserId), followData)
            set(targetUserRef.collection("followers").document(currentUserId), followData)
            update(currentUserRef, "followingCount", FieldValue.increment(1))
            update(targetUserRef, "followersCount", FieldValue.increment(1))

            val notification = mapOf(
                "recipientId" to targetUserId,
                "actorId" to currentUserId,
                "actorName" to currentUserName,
                "actorProfileImageUrl" to currentUserImageUrl,
                "type" to "NEW_FOLLOWER",
                "timestamp" to FieldValue.serverTimestamp(),
                "isRead" to false
            )

            set(notificationRef, notification)

        }.commit().await()
    }

    override suspend fun unfollowUser(currentUserId: String, targetUserId: String) {
        val currentUserRef = usersCollection.document(currentUserId)
        val targetUserRef = usersCollection.document(targetUserId)

        val notificationId = "${targetUserId}_${currentUserId}"
        val notificationRef = firestore.collection("notifications").document(notificationId)

        firestore.batch().apply {
            delete(currentUserRef.collection("following").document(targetUserId))
            delete(targetUserRef.collection("followers").document(currentUserId))
            update(currentUserRef, "followingCount", FieldValue.increment(-1))
            update(targetUserRef, "followersCount", FieldValue.increment(-1))
            delete(notificationRef)
        }.commit().await()
    }

    override fun isFollowing(currentUserId: String, targetUserId: String): Flow<Boolean> = callbackFlow {
        if (currentUserId.isEmpty() || targetUserId.isEmpty()) {
            trySend(false)
            awaitClose()
            return@callbackFlow
        }

        val followingRef = usersCollection.document(currentUserId)
            .collection("following").document(targetUserId)

        val subscription = followingRef.addSnapshotListener { snapshot, _ ->
            trySend(snapshot != null && snapshot.exists())
        }

        awaitClose { subscription.remove() }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getFollowersList(userId: String): Flow<List<User>> {
        return firestore.collection("users").document(userId).collection("followers")
            .snapshots().flatMapLatest { snapshot ->
                val followerIds = snapshot.documents.map { it.id }
                if (followerIds.isNotEmpty()) {
                    usersCollection.whereIn("id", followerIds).snapshots().map { usersSnapshot ->
                        usersSnapshot.toObjects(User::class.java)
                    }
                } else {
                    flowOf(emptyList())
                }
            }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getFollowingList(userId: String): Flow<List<User>> {
        return firestore.collection("users").document(userId).collection("following")
            .snapshots().flatMapLatest { snapshot ->
                val followingIds = snapshot.documents.map { it.id }
                if (followingIds.isNotEmpty()) {
                    usersCollection.whereIn("id", followingIds).snapshots().map { usersSnapshot ->
                        usersSnapshot.toObjects(User::class.java)
                    }
                } else {
                    flowOf(emptyList())
                }
            }
    }



}