package com.example.mygamelist.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mygamelist.data.model.Game
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.mygamelist.data.repository.GameRepository
import com.example.mygamelist.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject


private const val CACHE_STALE_TIME_MS = 24 * 60 * 60 * 1000L

data class HomeScreenState(
    val newReleaseGames: List<Game> = emptyList(),
    val newReleaseLoading: Boolean = false,
    val newReleaseError: String? = null,

    val comingSoonGames: List<Game> = emptyList(),
    val comingSoonLoading: Boolean = false,
    val comingSoonError: String? = null,

    val userSavedGameIds: Set<Int> = emptySet()
)

sealed class HomeUiEvent {
    data class ShowToast(val message: String) : HomeUiEvent()
    data class NavigateToAddGameForm(val gameId: Int) : HomeUiEvent()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val gameRepository: GameRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeScreenState())
    val uiState: StateFlow<HomeScreenState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<HomeUiEvent>()
    val events: SharedFlow<HomeUiEvent> = _events.asSharedFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _userSavedGamesFlow = authRepository.getCurrentUser()
        .filterNotNull()
        .flatMapLatest { firebaseUser ->
            gameRepository.getAllUserSavedGames(firebaseUser.uid)
        }
        .catch { e ->
            Log.e("HomeViewModel", "Error observing user saved games: ${e.localizedMessage}", e)
        }

    init {
        observeNewReleaseGames()
        observeComingSoonGames()
        observeUserSavedGames()

        loadNewReleaseGames(forceRefresh = false)
        loadComingSoonGames(forceRefresh = false)
    }

    private fun observeNewReleaseGames() {
        gameRepository.getNewReleaseGames()
            .onEach { games ->
                _uiState.value = _uiState.value.copy(
                    newReleaseGames = games,
                    newReleaseLoading = false,
                    newReleaseError = null
                )
            }
            .catch { e ->
                Log.e("HomeViewModel", "Error observing New Releases from DB: ${e.localizedMessage}", e)
                _uiState.value = _uiState.value.copy(
                    newReleaseLoading = false,
                    newReleaseError = e.localizedMessage ?: "Erro ao carregar novos lançamentos do cache."
                )
            }
            .launchIn(viewModelScope)
    }

    private fun observeComingSoonGames() {
        gameRepository.getComingSoonGames()
            .onEach { games ->
                _uiState.value = _uiState.value.copy(
                    comingSoonGames = games,
                    comingSoonLoading = false,
                    comingSoonError = null
                )
            }
            .catch { e ->
                Log.e("HomeViewModel", "Error observing Coming Soon from DB: ${e.localizedMessage}", e)
                _uiState.value = _uiState.value.copy(
                    comingSoonLoading = false,
                    comingSoonError = e.localizedMessage ?: "Erro ao carregar jogos em breve do cache."
                )
            }
            .launchIn(viewModelScope)
    }

    private fun observeUserSavedGames() {
        viewModelScope.launch {
            _userSavedGamesFlow
                .map { games -> games.map { it.id }.toSet() }
                .onEach { savedIds ->
                    _uiState.value = _uiState.value.copy(userSavedGameIds = savedIds)
                }
                .catch { e ->
                    Log.e("HomeViewModel", "Error observing user saved games: ${e.localizedMessage}", e)
                }
                .launchIn(viewModelScope)
        }
    }

    fun loadNewReleaseGames(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(newReleaseLoading = true, newReleaseError = null)

            try {
                val cachedGames = gameRepository.getNewReleaseGames().first()

                val isCacheStale = cachedGames.isEmpty() ||
                        cachedGames.any { game ->
                            game.lastUpdated == null || (System.currentTimeMillis() - game.lastUpdated) > CACHE_STALE_TIME_MS
                        }

                if (forceRefresh || isCacheStale) {
                    Log.d("HomeViewModel", "Cache de Novos Lançamentos vazio ou stale. Atualizando da API.")
                    gameRepository.refreshNewReleaseGamesCache()
                } else {
                    Log.d("HomeViewModel", "Cache de Novos Lançamentos válido. Usando dados cacheados.")
                    _uiState.value = _uiState.value.copy(newReleaseLoading = false, newReleaseError = null)
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error refreshing New Releases: ${e.localizedMessage}", e)
                _uiState.value = _uiState.value.copy(
                    newReleaseLoading = false,
                    newReleaseError = e.localizedMessage ?: "Erro ao carregar novos lançamentos."
                )
            }
        }
    }

    fun loadComingSoonGames(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(comingSoonLoading = true, comingSoonError = null)

            try {
                val cachedGames = gameRepository.getComingSoonGames().first()

                val isCacheStale = cachedGames.isEmpty() ||
                        cachedGames.any { game ->
                            game.lastUpdated == null || (System.currentTimeMillis() - game.lastUpdated) > CACHE_STALE_TIME_MS
                        }

                if (forceRefresh || isCacheStale) {
                    Log.d("HomeViewModel", "Cache de Em Breve vazio ou stale. Atualizando da API.")
                    gameRepository.refreshComingSoonGamesCache()
                } else {
                    Log.d("HomeViewModel", "Cache de Em Breve válido. Usando dados cacheados.")
                    _uiState.value = _uiState.value.copy(comingSoonLoading = false, comingSoonError = null)
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Erro ao refrescar Coming Soon: ${e.localizedMessage}", e)
                _uiState.value = _uiState.value.copy(
                    comingSoonLoading = false,
                    comingSoonError = e.localizedMessage ?: "Erro ao carregar jogos em breve."
                )
            }
        }
    }

    fun toggleGameInUserList(game: Game) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: run {
                _events.emit(HomeUiEvent.ShowToast("Erro: Usuário não logado."))
                return@launch
            }
            val isCurrentlyAdded = gameRepository.isGameSavedLocally(game.id, userId)
            try {
                if (isCurrentlyAdded) {
                    gameRepository.removeGameFromUserList(game, userId)
                    _events.emit(HomeUiEvent.ShowToast("Jogo '${game.title}' removido da sua lista."))
                } else {
                    _events.emit(HomeUiEvent.NavigateToAddGameForm(game.id))
                }
            } catch (e: Exception) {
                _events.emit(HomeUiEvent.ShowToast("Erro ao alterar status do jogo: ${e.localizedMessage}"))
                e.printStackTrace()
            }
        }
    }
}