package com.example.mygamelist.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mygamelist.data.model.Notification
import com.example.mygamelist.data.repository.AuthRepository
import com.example.mygamelist.data.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationsUiState(
    val isLoading: Boolean = true,
    val notifications: List<Notification> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    notificationRepository: NotificationRepository,
    authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init {
        val userId = authRepository.getCurrentUserId()
        if (userId != null) {
            viewModelScope.launch {
                notificationRepository.getNotifications(userId)
                    .catch { e ->
                        _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
                    }
                    .collect { notifications ->
                        _uiState.update { it.copy(isLoading = false, notifications = notifications) }
                    }
            }
        } else {
            _uiState.update { it.copy(isLoading = false, error = "Usuário não logado.") }
        }
    }
}