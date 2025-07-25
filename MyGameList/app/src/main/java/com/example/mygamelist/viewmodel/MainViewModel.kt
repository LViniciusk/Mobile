package com.example.mygamelist.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mygamelist.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed class AuthVerificationState {
    object Loading : AuthVerificationState()
    object Authenticated : AuthVerificationState()
    object Unauthenticated : AuthVerificationState()
}

@HiltViewModel
class MainViewModel @Inject constructor(
    authRepository: AuthRepository
) : ViewModel() {

    val authState = authRepository.getCurrentUser().map { user ->
        if (user != null) {
            AuthVerificationState.Authenticated
        } else {
            AuthVerificationState.Unauthenticated
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AuthVerificationState.Loading
    )
}