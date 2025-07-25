package com.example.mygamelist.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mygamelist.data.model.User
import com.example.mygamelist.data.repository.AuthRepository
import com.example.mygamelist.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CommunityUser(
    val user: User,
    val isFollowedByCurrentUser: Boolean
)

data class CommunityUiState(
    val isLoading: Boolean = true,
    val displayedUsers: List<CommunityUser> = emptyList(),
    val searchQuery: String = "",
    val error: String? = null
)

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommunityUiState())
    val uiState: StateFlow<CommunityUiState> = _uiState.asStateFlow()

    private val currentUserId: String = authRepository.getCurrentUserId() ?: ""
    private var currentUser: User? = null
    private var allUsersCache: List<CommunityUser> = emptyList()

    init {
        observeCommunityData()
    }

    private fun observeCommunityData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            currentUser = userRepository.getUserProfile(currentUserId).first()
            if (currentUser == null) {
                _uiState.update { it.copy(isLoading = false, error = "Usuário atual não encontrado.") }
                return@launch
            }

            val allUsers = userRepository.getAllUsers().filter { it.id != currentUserId }

            userRepository.getFollowingIds(currentUserId).collect { followingIds ->
                val communityUsers = allUsers.map { user ->
                    CommunityUser(
                        user = user,
                        isFollowedByCurrentUser = followingIds.contains(user.id)
                    )
                }

                allUsersCache = communityUsers
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        displayedUsers = filterUsers(it.searchQuery)
                    )
                }
            }
        }
    }

    fun followUser(targetUser: User) {
        viewModelScope.launch {
            val loggedInUser = currentUser ?: return@launch
            try {
                userRepository.followUser(
                    currentUserId = loggedInUser.id,
                    currentUserName = loggedInUser.name,
                    currentUserImageUrl = loggedInUser.profileImageUrl,
                    targetUserId = targetUser.id
                )
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Erro ao seguir: ${e.message}") }
            }
        }
    }

    fun unfollowUser(targetUser: User) {
        viewModelScope.launch {
            try {
                userRepository.unfollowUser(currentUserId, targetUser.id)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Erro ao deixar de seguir: ${e.message}") }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update {
            it.copy(
                searchQuery = query,
                displayedUsers = filterUsers(query)
            )
        }
    }

    private fun filterUsers(query: String): List<CommunityUser> {
        return if (query.isBlank()) {
            allUsersCache
        } else {
            allUsersCache.filter { communityUser ->
                communityUser.user.name.contains(query, ignoreCase = true) ||
                        communityUser.user.username.contains(query, ignoreCase = true)
            }
        }
    }
}