package com.example.mygamelist.viewmodel

import android.util.Log
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mygamelist.data.model.Game
import com.example.mygamelist.data.model.GameStatus
import com.example.mygamelist.data.model.toGameEntity
import com.example.mygamelist.data.repository.AuthRepository
import com.example.mygamelist.data.repository.GameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


data class AddGameFormScreenState(
    val game: Game? = null,
    val selectedStatus: GameStatus = GameStatus.NONE,
    val userRating: Int? = 0,
    val userReview: TextFieldValue = TextFieldValue(""),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val isGameInList: Boolean = false
)

sealed class AddGameFormUiEvent {
    data class ShowToast(val message: String) : AddGameFormUiEvent()
    object NavigateBack : AddGameFormUiEvent()
}


@HiltViewModel
class AddGameFormViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val gameRepository: GameRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddGameFormScreenState())
    val uiState: StateFlow<AddGameFormScreenState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AddGameFormUiEvent>()
    val events: SharedFlow<AddGameFormUiEvent> = _events.asSharedFlow()

    init {
        val gameId = savedStateHandle.get<Int>("gameId")
        if (gameId != null && gameId != -1) {
            loadGameDetails(gameId)
        } else {
            _uiState.value = _uiState.value.copy(
                error = "ID do jogo não fornecido para o formulário."
            )
        }
    }


    fun loadGameDetails(gameId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val userId = authRepository.getCurrentUserId() ?: run {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Usuário não logado para carregar detalhes do jogo."
                )
                return@launch
            }
            try {
                val gameDetail = gameRepository.getGameDetails(gameId)
                val existingGame = gameRepository.getUserGameById(gameId, userId)

                _uiState.value = _uiState.value.copy(
                    game = gameDetail.toGameEntity(),
                    selectedStatus = existingGame?.status ?: GameStatus.NONE,
                    userRating = existingGame?.userRating,
                    isGameInList = existingGame != null,
                    userReview = existingGame?.userReview?.let { TextFieldValue(it) } ?: TextFieldValue(""),
                    isLoading = false
                )
            } catch (e: Exception) {
                Log.e("AddGameFormViewModel", "Erro ao carregar detalhes do jogo para o formulário: ${e.localizedMessage}", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.localizedMessage ?: "Erro ao carregar detalhes do jogo para o formulário."
                )
            }
        }
    }

    fun removeUserGame() {
        viewModelScope.launch {
            val gameToRemove = uiState.value.game ?: return@launch
            val userId = authRepository.getCurrentUserId() ?: return@launch

            try {
                gameRepository.removeGameFromUserList(gameToRemove, userId)
                _events.emit(AddGameFormUiEvent.ShowToast("'${gameToRemove.title}' removido da sua lista."))
                _events.emit(AddGameFormUiEvent.NavigateBack)
            } catch (e: Exception) {
                _events.emit(AddGameFormUiEvent.ShowToast("Erro ao remover o jogo: ${e.localizedMessage}"))
            }
        }
    }

    fun onStatusSelected(status: GameStatus) {
        _uiState.value = _uiState.value.copy(selectedStatus = status)
    }

    fun onRatingChanged(rating: Int?) {
        _uiState.value = _uiState.value.copy(userRating = rating)
    }

    fun onReviewChanged(review: TextFieldValue) {
        _uiState.value = _uiState.value.copy(userReview = review)
    }


    fun saveUserGame() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null, saveSuccess = false)
            val currentState = _uiState.value
            val gameToSave = currentState.game ?: run {
                _uiState.value = currentState.copy(
                    isSaving = false,
                    error = "Jogo base não encontrado para salvar."
                )
                return@launch
            }

            if (currentState.selectedStatus == GameStatus.NONE) {
                _events.emit(AddGameFormUiEvent.ShowToast("Por favor, selecione um status."))
                _uiState.value = currentState.copy(isSaving = false)
                return@launch
            }

            val userId = authRepository.getCurrentUserId() ?: run {
                _events.emit(AddGameFormUiEvent.ShowToast("Erro: Usuário não logado para salvar jogo."))
                _uiState.value = currentState.copy(isSaving = false)
                return@launch
            }

            try {
                val updatedGame = gameToSave.copy(
                    status = currentState.selectedStatus,
                    userRating = currentState.userRating,
                    userReview = currentState.userReview.text,
                    userId = userId
                )
                gameRepository.addGameToUserList(updatedGame, userId)
                _uiState.value = currentState.copy(isSaving = false, saveSuccess = true)
                val message = if (currentState.isGameInList) "atualizado" else "adicionado"
                _events.emit(AddGameFormUiEvent.ShowToast("Jogo '${updatedGame.title}' $message com sucesso!"))
                _events.emit(AddGameFormUiEvent.NavigateBack)
            } catch (e: Exception) {
                Log.e("AddGameFormViewModel", "Erro ao salvar jogo: ${e.localizedMessage}", e)
                _uiState.value = currentState.copy(
                    isSaving = false,
                    error = e.localizedMessage ?: "Erro ao salvar o jogo."
                )
                _events.emit(AddGameFormUiEvent.ShowToast("Erro ao salvar jogo: ${e.localizedMessage}"))
            }
        }
    }
}