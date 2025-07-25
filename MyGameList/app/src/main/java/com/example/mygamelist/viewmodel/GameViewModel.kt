package com.example.mygamelist.viewmodel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mygamelist.data.api.RawgService
import com.example.mygamelist.data.model.GameResult
import com.example.mygamelist.data.repository.GameRepository
import com.example.mygamelist.data.model.toDomainGame
import com.example.mygamelist.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

sealed class GameUiState {
    object Idle : GameUiState()
    object Loading : GameUiState()
    data class Success(val games: List<GameResult>, val userSavedGameIds: Set<Int>) : GameUiState()
    data class Error(val message: String) : GameUiState()
}

sealed class GameUiEvent {
    data class ShowToast(val message: String) : GameUiEvent()
}

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class GameViewModel @Inject constructor(
    private val rawgService: RawgService,
    private val gameRepository: GameRepository,
    private val authRepository: AuthRepository,
    @Named("rawg_api_key") private val apiKey: String
) : ViewModel() {

    private val _uiState = mutableStateOf<GameUiState>(GameUiState.Idle)
    val uiState: State<GameUiState> get() = _uiState

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _events = MutableSharedFlow<GameUiEvent>()
    val events: SharedFlow<GameUiEvent> = _events.asSharedFlow()

    private val gameCache = mutableMapOf<String, List<GameResult>>()

    private val _userSavedGamesFlow = authRepository.getCurrentUser()
        .filterNotNull()
        .flatMapLatest { firebaseUser ->
            gameRepository.getAllUserSavedGames(firebaseUser.uid)
        }
        .catch { e ->
            Log.e("GameViewModel", "Error observing user saved games: ${e.localizedMessage}", e)
        }

    init {
        _searchQuery
            .debounce(500L)
            .filter { it.length >= 3 || it.isEmpty() }
            .distinctUntilChanged()
            .flatMapLatest { query ->
                if (query.isBlank()) {
                    _uiState.value = GameUiState.Idle
                    flowOf(emptyList())
                } else {
                    _uiState.value = GameUiState.Loading
                    try {
                        val results = if (gameCache.containsKey(query)) {
                            gameCache[query] ?: emptyList()
                        } else {
                            val response = rawgService.searchGames(apiKey, query)
                            gameCache[query] = response.results
                            response.results
                        }
                        flowOf(results)
                    } catch (_: Exception) {
                        flowOf(emptyList())
                    }
                }
            }
            .combine(_userSavedGamesFlow) { gameResults, userSavedGames ->
                val userSavedIds = userSavedGames.map { it.id }.toSet()
                Pair(gameResults, userSavedIds)
            }
            .catch { e ->
                _uiState.value = GameUiState.Error(e.localizedMessage ?: "Erro desconhecido")
                e.printStackTrace()
            }
            .onEach { (gameResults, userSavedIds) ->
                if (_searchQuery.value.isNotBlank()) {
                    _uiState.value = GameUiState.Success(gameResults, userSavedIds)
                }
            }
            .launchIn(viewModelScope)
    }


    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        if (query.length < 3 && query.isNotBlank()) {
            _uiState.value = GameUiState.Idle
        } else if (query.isBlank()) {
            _uiState.value = GameUiState.Idle
        }
    }

    fun searchGames(query: String) {
        viewModelScope.launch {
            _uiState.value = GameUiState.Loading

            try {
                var results: List<GameResult> = emptyList()
                if (gameCache.containsKey(query)) {
                    results = gameCache[query] ?: emptyList()
                } else {
                    val response = rawgService.searchGames(apiKey, query)
                    gameCache[query] = results
                    results = response.results
                }

                val userSavedIds = _userSavedGamesFlow.first().map { it.id }.toSet()
                _uiState.value = GameUiState.Success(results, userSavedIds)
            } catch (e: Exception) {
                _uiState.value = GameUiState.Error("Falha ao buscar jogos: ${e.message}")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun toggleGameInUserList(gameResult: GameResult, isCurrentlyAdded: Boolean) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: run {
                _events.emit(GameUiEvent.ShowToast("Erro: Usuário não logado."))
                return@launch
            }
            try {
                val game = gameResult.toDomainGame()
                if (isCurrentlyAdded) {
                    gameRepository.removeGameFromUserList(game, userId)
                    _events.emit(GameUiEvent.ShowToast("Jogo '${game.title}' removido da sua lista."))
                }
            } catch (e: Exception) {
                _events.emit(GameUiEvent.ShowToast("Erro ao alterar status do jogo: ${e.localizedMessage}"))
                e.printStackTrace()
            }
        }
    }
}