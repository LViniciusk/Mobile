package com.example.authapp2.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.authapp2.model.repository.AuthRepository
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import kotlinx.coroutines.launch




class AuthViewModel (private val repository : AuthRepository ) : ViewModel() {
    var loginResult : ((Boolean) -> Unit)? = null
    var registerResult : ((Boolean) -> Unit)? = null
    fun login(email: String, password : String, onResult : (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository .loginUser( email, password )
            onResult (success) // Retorna true ou false para a tela de login
        }
    }
    fun resetPassword (email: String, onResult : (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository .resetPassword( email)
            onResult (success)
        }
    }
    fun getUserName (onResult : (String?) -> Unit) {
        viewModelScope.launch {
            val name = repository .getUserName()
            onResult (name)
        }
    }
    fun loginWithGoogle (idToken: String, onResult : (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository .loginWithGoogle( idToken)
            onResult (success)
        }
    }
    fun getGoogleSignInClient (context: Context): GoogleSignInClient {
        return repository .getGoogleSignInClient( context)
    }
    fun logout() {
        repository .logout()
    }
    fun register (email: String, password : String, name: String, onResult : (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository .registerUser( email, password , name)
            onResult (success)
        }
    }
}

class AuthViewModelFactory(private val repository: AuthRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}