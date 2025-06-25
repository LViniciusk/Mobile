package com.example.mygamelist.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mygamelist.data.model.Game
import com.example.mygamelist.data.repository.GameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
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
    private val _events = MutableSharedFlow<HomeUiEvent>()
    val events: SharedFlow<HomeUiEvent> = _events.asSharedFlow()

    init {
        loadAllGames()
    }

    fun loadAllGames() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)


            val newReleasesJob = launch {
                gameRepository.getNewReleaseGames()
                    .catch { e ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = e.localizedMessage ?: "Erro ao carregar novos lançamentos."
                        )
                        e.printStackTrace()
                    }
                    .collect { games ->
                        _uiState.value = _uiState.value.copy(
                            newReleaseGames = games,
                            isLoading = false
                        )
                    }
            }

            val comingSoonJob = launch {
                gameRepository.getComingSoonGames()
                    .catch { e ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = _uiState.value.error ?: (e.localizedMessage ?: "Erro ao carregar jogos em breve.")
                        )
                        e.printStackTrace()
                    }
                    .collect { games ->
                        _uiState.value = _uiState.value.copy(
                            comingSoonGames = games,
                            isLoading = false
                        )
                    }
            }
            newReleasesJob.join()
            comingSoonJob.join()
        }
    }

    fun addGameToUserGames(game: Game) {
        viewModelScope.launch {
            _events.emit(HomeUiEvent.ShowToast("Adicionando ${game.title} à sua lista!"))
        }
    }
}

sealed class HomeUiEvent {
    data class ShowToast(val message: String) : HomeUiEvent()
}