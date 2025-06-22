package com.example.mygamelist.viewmodel


import com.example.mygamelist.data.model.GameResult
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mygamelist.data.api.RetrofitClient
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

private const val API_KEY = "84f487f7f7ce4190a0911d867db3c1ef"

sealed class GameUiState {
    object Idle : GameUiState()
    object Loading : GameUiState()
    data class Success(val games: List<GameResult>) : GameUiState()
    data class Error(val message: String) : GameUiState()
}



@OptIn(FlowPreview::class)
class GameViewModel : ViewModel() {

    private val _uiState = mutableStateOf<GameUiState>(GameUiState.Idle)
    val uiState: State<GameUiState> get() = _uiState

    private val _searchQuery = MutableStateFlow("")

    private val gameCache = mutableMapOf<String, List<GameResult>>()

    init {
        viewModelScope.launch {
            _searchQuery
                .filter { it.length >= 3 }
                .debounce(500L)
                .distinctUntilChanged()
                .onEach { query ->
                    searchGames(query)
                }
                .launchIn(viewModelScope)
        }
    }


    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        if (query.length < 3) {
            _uiState.value = GameUiState.Idle
        }
    }

    private fun searchGames(query: String) {
        viewModelScope.launch {
            if (gameCache.containsKey(query)) {
                _uiState.value = GameUiState.Success(gameCache[query] ?: emptyList())
                return@launch
            }

            _uiState.value = GameUiState.Loading

            try {
                val results = RetrofitClient.api.searchGames(API_KEY, query).results
                gameCache[query] = results
                _uiState.value = GameUiState.Success(results)
            } catch (e: Exception) {
                _uiState.value = GameUiState.Error("Falha ao buscar jogos: ${e.message}")
            }
        }
    }
}