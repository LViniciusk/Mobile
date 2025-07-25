package com.example.mygamelist.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mygamelist.data.model.User
import com.example.mygamelist.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FollowListUiState(
    val isLoading: Boolean = true,
    val profileUser: User? = null,
    val followers: List<User> = emptyList(),
    val following: List<User> = emptyList(),
    val initialTab: Int = 0
)

@HiltViewModel
class FollowListViewModel @Inject constructor(
    private val userRepository: UserRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(FollowListUiState())
    val uiState: StateFlow<FollowListUiState> = _uiState.asStateFlow()

    init {
        val userId: String = checkNotNull(savedStateHandle["userId"])
        val initialTab: Int = checkNotNull(savedStateHandle["initialTab"])

        _uiState.update { it.copy(initialTab = initialTab) }

        viewModelScope.launch {
            userRepository.getUserProfile(userId).first { user ->
                _uiState.update { it.copy(profileUser = user) }
                user != null
            }
        }

        viewModelScope.launch {
            userRepository.getFollowersList(userId).collect { followers ->
                _uiState.update { it.copy(isLoading = false, followers = followers) }
            }
        }
        viewModelScope.launch {
            userRepository.getFollowingList(userId).collect { following ->
                _uiState.update { it.copy(isLoading = false, following = following) }
            }
        }
    }
}