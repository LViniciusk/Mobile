package com.example.mygamelist.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mygamelist.data.model.GameDetail
import com.example.mygamelist.data.model.toGameEntity
import com.example.mygamelist.data.repository.AuthRepository
import com.example.mygamelist.data.repository.GameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


data class GameDetailScreenState(
    val gameDetail: GameDetail? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isGameInUserList: Boolean = false
)

sealed class GameDetailUiEvent {
    data class ShowToast(val message: String) : GameDetailUiEvent()
    data class NavigateToAddGameForm(val gameId: Int) : GameDetailUiEvent()
    object NavigateBack : GameDetailUiEvent()
}


@HiltViewModel
class GameDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val gameRepository: GameRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    internal val _uiState = MutableStateFlow(GameDetailScreenState())
    val uiState: StateFlow<GameDetailScreenState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<GameDetailUiEvent>()
    val events: SharedFlow<GameDetailUiEvent> = _events.asSharedFlow()

    private var currentLoadedGameId: Int? = null

    init {
        val gameId = savedStateHandle.get<Int>("gameId")
        if (gameId != null) {
            currentLoadedGameId = gameId
            loadGameDetails(gameId)
        } else {
            _uiState.value = _uiState.value.copy(
                error = "ID do jogo não fornecido para detalhes."
            )
        }
    }

    fun loadGameDetails(gameId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val userId = authRepository.getCurrentUserId()
            if (userId == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Usuário não logado para carregar status do jogo."
                )
                return@launch
            }
            try {
                val details = gameRepository.getGameDetails(gameId)
                val isInList = gameRepository.isGameSavedLocally(gameId, userId)

                _uiState.value = _uiState.value.copy(
                    gameDetail = details,
                    isLoading = false,
                    isGameInUserList = isInList
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.localizedMessage ?: "Erro ao carregar detalhes do jogo."
                )
                e.printStackTrace()
            }
        }
    }

    fun toggleGameInUserList(game: GameDetail) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: run {
                _events.emit(GameDetailUiEvent.ShowToast("Erro: Usuário não logado."))
                return@launch
            }
            val currentStatus = _uiState.value.isGameInUserList
            try {
                if (currentStatus) {
                    gameRepository.removeGameFromUserList(game.toGameEntity(), userId)
                    _uiState.value = _uiState.value.copy(isGameInUserList = false)
                    _events.emit(GameDetailUiEvent.ShowToast("Jogo '${game.title}' removido da sua lista."))
                } else {
                    _events.emit(GameDetailUiEvent.NavigateToAddGameForm(game.id))
                }
            } catch (e: Exception) {
                _events.emit(GameDetailUiEvent.ShowToast("Erro ao alterar status do jogo: ${e.localizedMessage}"))
                e.printStackTrace()
            }
        }
    }


}