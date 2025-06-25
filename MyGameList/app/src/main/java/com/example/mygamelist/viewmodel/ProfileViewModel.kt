package com.example.mygamelist.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mygamelist.R
import com.example.mygamelist.data.model.Game
import com.example.mygamelist.data.model.GameStatus
import com.example.mygamelist.data.model.User
import com.example.mygamelist.data.model.UserStats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileScreenState(
    val user: User? = null,
    val userGames: List<Game> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileScreenState())
    private val _events = MutableSharedFlow<ProfileUiEvent>()
    val uiState: StateFlow<ProfileScreenState> = _uiState.asStateFlow()
    val events: SharedFlow<ProfileUiEvent> = _events.asSharedFlow()

    init {
        loadUserProfile()
        observeUiStateAndFilterGames()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val currentUser = User(
                    id = 1,
                    name = "Vinicius",
                    username = "lviniciusk",
                    quote = "jalp",
                    stats = UserStats(
                        todos = 6,
                        concluidos = 2,
                        jogando = 2,
                        abandonados = 1,
                        desejados = 1
                    ),
                    avatarUrl = "https://mygamelist.club/avatars/682f8010da749fdfd6e1f9f1-Hb41cgGP",
                    userGames = listOf(
                        Game(1, "The Witcher 3: Wild Hunt", "https://media.rawg.io/media/games/618/618c2031a07bbff6b4f611f10b6bcdbc.jpg", "2015", 92, "RPG", GameStatus.JOGANDO),
                        Game(2, "Grand Theft Auto V", "https://media.rawg.io/media/games/20a/20aa03a10cda45239fe22d035c0ebe64.jpg", "2013", 92, "Action", GameStatus.CONCLUIDO),
                        Game(3, "Portal 2", "https://media.rawg.io/media/games/2ba/2bac0e87cf45e5b508f227d281c9252a.jpg", "2011", 95, "Puzzle", GameStatus.JOGANDO),
                        Game(4, "Red Dead Redemption 2", "https://media.rawg.io/media/games/511/5118aff5091cb3efec399c808f8c598f.jpg", "2018", 96, "Action", GameStatus.DESEJO),
                        Game(5, "The Elder Scrolls V: Skyrim", "https://media.rawg.io/media/games/7cf/7cfc9220b401b7a300e409e539c9afd5.jpg", "2011", 94, "RPG", GameStatus.CONCLUIDO),
                        Game(6, "Life is Strange", "https://media.rawg.io/media/games/562/562553814dd54e001a541e4ee83a591c.jpg", "2015", 83, "Adventure", GameStatus.ABANDONADO)
                    ),
                    followersCount = 0,
                    followingCount = 0
                )

                _uiState.value = _uiState.value.copy(
                    user = currentUser,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Falha ao carregar perfil: ${e.localizedMessage}"
                )
                e.printStackTrace()
            }
        }
    }

    private fun observeUiStateAndFilterGames() {
        viewModelScope.launch {

            _uiState
                .distinctUntilChanged { old, new ->

                    old.searchQuery == new.searchQuery && old.user?.userGames == new.user?.userGames
                }
                .collect { currentState ->

                    val rawGames = currentState.user?.userGames ?: emptyList()

                    val filteredGames = if (currentState.searchQuery.isBlank()) {
                        rawGames
                    } else {
                        rawGames.filter {
                            it.title.contains(currentState.searchQuery, ignoreCase = true)
                        }
                    }

                    if (filteredGames != currentState.userGames) {
                        _uiState.value = currentState.copy(userGames = filteredGames)
                    }
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }


    fun onDownloadListClick() {
        viewModelScope.launch {
            _events.emit(ProfileUiEvent.ShowToast("Baixando a Lista")) // <-- Emite o evento
        }
    }
}

sealed class ProfileUiEvent {
    data class ShowToast(val message: String) : ProfileUiEvent()
}