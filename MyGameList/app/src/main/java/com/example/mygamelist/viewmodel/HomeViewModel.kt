package com.example.mygamelist.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mygamelist.data.model.Game
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.mygamelist.data.repository.GameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import javax.inject.Inject

data class HomeScreenState(
    val newReleaseGames: List<Game> = emptyList(),
    val comingSoonGames: List<Game> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val gameRepository: GameRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeScreenState())
    val uiState: StateFlow<HomeScreenState> = _uiState.asStateFlow()

    init {
        loadNewReleaseGames()
    }

    private fun loadNewReleaseGames() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            gameRepository.getNewReleaseGames()
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.localizedMessage ?: "Erro desconhecido ao carregar jogos."
                    )
                    e.printStackTrace()
                }
                .collect { games ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        newReleaseGames = games
                    )
                }

            gameRepository.getComingSoonGames()
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.localizedMessage ?: "Erro desconhecido ao carregar jogos."
                    )
                    e.printStackTrace()
                }
                .collect { games ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        comingSoonGames = games
                    )
                }
        }
    }
}