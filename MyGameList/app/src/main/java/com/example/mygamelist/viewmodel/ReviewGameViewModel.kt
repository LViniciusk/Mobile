package com.example.mygamelist.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mygamelist.data.model.Game
import com.example.mygamelist.data.model.User
import com.example.mygamelist.data.repository.AuthRepository
import com.example.mygamelist.data.repository.GameRepository
import com.example.mygamelist.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReviewGameUiState(
    val game: Game? = null,
    val user: User? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val isMyReview: Boolean = false
)

@HiltViewModel
class ReviewGameViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val gameRepository: GameRepository,
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewGameUiState())
    val uiState: StateFlow<ReviewGameUiState> = _uiState.asStateFlow()

    init {
        val gameId: Int = checkNotNull(savedStateHandle["gameId"])
        val ownerId: String = checkNotNull(savedStateHandle["userId"])

        val currentUserId = authRepository.getCurrentUserId()

        _uiState.update { it.copy(isMyReview = ownerId == currentUserId) }

        loadData(gameId, ownerId)
    }

    private fun loadData(gameId: Int, ownerId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val userGame = gameRepository.getUserGameById(gameId, ownerId)
                val userProfile = userRepository.getUserProfile(ownerId).first()

                _uiState.update { it.copy(
                    game = userGame,
                    user = userProfile,
                    isLoading = false
                )}
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
            }
        }
    }
}