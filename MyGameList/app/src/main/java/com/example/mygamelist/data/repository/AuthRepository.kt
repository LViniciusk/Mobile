package com.example.mygamelist.data.repository

import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose
import javax.inject.Inject


interface AuthRepository {
    suspend fun register(email: String, password: String): String
    suspend fun login(email: String, password: String): String
    fun logout()
    fun getCurrentUser(): Flow<FirebaseUser?>
    fun getCurrentUserId(): String?
    suspend fun signInWithGoogle(idToken: String): String
}



class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val userRepository: UserRepository
) : AuthRepository {

    override suspend fun register(email: String, password: String): String {
        return firebaseAuth.createUserWithEmailAndPassword(email, password)
            .await()
            .user?.uid ?: throw Exception("Falha ao obter UID do usuário após registro.")
    }


    override suspend fun login(email: String, password: String): String {
        return firebaseAuth.signInWithEmailAndPassword(email, password)
            .await()
            .user?.uid ?: throw Exception("Falha ao obter UID do usuário após login.")
    }

    override fun logout() {
        firebaseAuth.signOut()
    }

    override fun getCurrentUser(): Flow<FirebaseUser?> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        firebaseAuth.addAuthStateListener(authStateListener)
        awaitClose {
            firebaseAuth.removeAuthStateListener(authStateListener)
        }
    }

    override fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }

    override suspend fun signInWithGoogle(idToken: String): String {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val authResult = firebaseAuth.signInWithCredential(credential).await()
        val user = authResult.user!!

        val isNewUser = authResult.additionalUserInfo?.isNewUser ?: false
        if (isNewUser) {
            userRepository.createInitialProfile(
                userId = user.uid,
                username = user.email?.substringBefore('@') ?: "user${user.uid.take(5)}",
                email = user.email ?: "",
                profileImageUrl = user.photoUrl?.toString()
            )
        }

        return user.uid
    }
}
