package com.example.mygamelist.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mygamelist.data.model.Game
import com.example.mygamelist.data.model.GameStatus
import com.example.mygamelist.data.model.User
import com.example.mygamelist.data.model.UserStats
import com.example.mygamelist.data.repository.AuthRepository
import com.example.mygamelist.data.repository.GameRepository
import com.example.mygamelist.data.repository.ImgurRepository
import com.example.mygamelist.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject


data class ProfileScreenState(
    val user: User? = null,
    val isMyProfile: Boolean = false,
    val isFollowing: Boolean = false,
    val userGames: List<Game> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val error: String? = null,
    val userStats: UserStats = UserStats(),
    val displayName: String = "",
    val bio: String = "",
    val selectedProfileImageUri: Uri? = null,
    val isSavingProfile: Boolean = false
)


sealed class ProfileUiEvent {
    data class ShowToast(val message: String) : ProfileUiEvent()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val gameRepository: GameRepository,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val imgurRepository: ImgurRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileScreenState())
    val uiState: StateFlow<ProfileScreenState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ProfileUiEvent>()
    val events: SharedFlow<ProfileUiEvent> = _events.asSharedFlow()

    private var dataLoadingJob: Job? = null

    init {
        viewModelScope.launch {
            authRepository.getCurrentUser().collect { firebaseUser ->
                dataLoadingJob?.cancel()

                if (firebaseUser == null) {
                    _uiState.value = ProfileScreenState(isLoading = false)
                } else {
                    reloadData()
                }
            }
        }
    }

    fun reloadData() {
        dataLoadingJob = viewModelScope.launch {
            val profileUserId: String? = savedStateHandle.get<String>("userId")
            val currentUserId = authRepository.getCurrentUserId() ?: ""
            val userIdToLoad = profileUserId ?: currentUserId

            if (userIdToLoad.isEmpty()) {
                _uiState.update { it.copy(isLoading = false, error = "Usuário não identificado.") }
                return@launch
            }

            _uiState.update { it.copy(
                isLoading = true,
                isMyProfile = profileUserId == null || profileUserId == currentUserId
            )}

            observeUserProfile(userIdToLoad)
            observeUserGamesAndFilter(userIdToLoad)

            if (!_uiState.value.isMyProfile) {
                observeFollowingStatus(currentUserId, userIdToLoad)
            }
        }
    }

    private fun observeFollowingStatus(currentUserId: String, targetUserId: String) {
        viewModelScope.launch {
            userRepository.isFollowing(currentUserId, targetUserId).collect { isFollowing ->
                _uiState.update { it.copy(isFollowing = isFollowing) }
            }
        }
    }

    fun follow() {
        viewModelScope.launch {
            val profileUserId = savedStateHandle.get<String>("userId")
            val currentUserId = authRepository.getCurrentUserId()
            if (currentUserId == null || profileUserId == null) return@launch

            try {
                val currentUser = userRepository.getUserProfile(currentUserId).first()
                if (currentUser != null) {
                    userRepository.followUser(
                        currentUserId = currentUser.id,
                        currentUserName = currentUser.name,
                        currentUserImageUrl = currentUser.profileImageUrl,
                        targetUserId = profileUserId
                    )
                }
            } catch (e: Exception) {
                _events.emit(ProfileUiEvent.ShowToast("Erro ao tentar seguir: ${e.message}"))
            }
        }
    }

    fun unfollow() {
        viewModelScope.launch {
            val profileUserId = savedStateHandle.get<String>("userId")
            val currentUserId = authRepository.getCurrentUserId()
            if (currentUserId == null || profileUserId == null) return@launch

            try {
                userRepository.unfollowUser(currentUserId, profileUserId)
            } catch (e: Exception) {
                _events.emit(ProfileUiEvent.ShowToast("Erro ao deixar de seguir: ${e.message}"))
            }
        }
    }

    private fun observeUserProfile(userId: String) {
        viewModelScope.launch {
            userRepository.getUserProfile(userId)
                .onEach { user ->
                    if (user != null) {
                        _uiState.update { it.copy(
                            user = user,
                            isLoading = false,
                            error = null,
                            displayName = user.name,
                            bio = user.quote
                        )}
                        Log.d("ProfileViewModel", "Perfil do usuário ${user.username} carregado.")
                    } else {
                        _uiState.update { it.copy(
                            isLoading = false,
                            error = "Perfil não encontrado no Firestore."
                        )}
                        Log.w("ProfileViewModel", "Perfil não encontrado para UID: $userId")
                    }
                }
                .catch { e ->
                    Log.e("ProfileViewModel", "Erro ao carregar perfil: ${e.localizedMessage}", e)
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = e.localizedMessage ?: "Erro ao carregar perfil."
                    )}
                }
                .launchIn(viewModelScope)
        }
    }

    private fun observeUserGamesAndFilter(userId: String) {
        viewModelScope.launch {
            gameRepository.getAllUserSavedGames(userId)
                .combine(_uiState.map { it.searchQuery }.distinctUntilChanged()) { savedGames, searchQuery ->
                    if (searchQuery.isBlank()) {
                        savedGames
                    } else {
                        savedGames.filter {
                            it.title.contains(searchQuery, ignoreCase = true)
                        }
                    }
                }
                .onEach { filteredGames ->
                    _uiState.update { it.copy(userGames = filteredGames) }
                    updateUserStats(filteredGames)
                }
                .catch { e ->
                    _uiState.update { it.copy(error = e.localizedMessage ?: "Erro ao carregar jogos salvos.") }
                }
                .launchIn(viewModelScope)
        }
    }

    private fun updateUserStats(games: List<Game>) {
        val allGames = games.size
        val completedGames = games.count { it.status == GameStatus.CONCLUIDO }
        val playingGames = games.count { it.status == GameStatus.JOGANDO }
        val droppedGames = games.count { it.status == GameStatus.ABANDONADO }
        val wishGames = games.count { it.status == GameStatus.DESEJO }

        _uiState.update { it.copy(
            userStats = UserStats(allGames, completedGames, playingGames, droppedGames, wishGames)
        )}
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onDownloadListClick() {
        viewModelScope.launch {
            _events.emit(ProfileUiEvent.ShowToast("Ação: Baixar lista de jogos do usuário. (TODO: Implementar)"))
        }
    }

    fun onDisplayNameChange(newName: String) {
        _uiState.update { it.copy(displayName = newName) }
    }

    fun onBioChange(newBio: String) {
        _uiState.update { it.copy(bio = newBio) }
    }

    fun onProfileImageSelected(uri: Uri?) {
        _uiState.update { it.copy(selectedProfileImageUri = uri) }
    }

    fun saveProfile() {
        viewModelScope.launch {
            if (!_uiState.value.isMyProfile) return@launch

            _uiState.update { it.copy(isSavingProfile = true, error = null) }
            val userId = authRepository.getCurrentUserId() ?: run {
                _uiState.update { it.copy(isSavingProfile = false, error = "Usuário não logado.") }
                _events.emit(ProfileUiEvent.ShowToast("Erro: Usuário não logado."))
                return@launch
            }

            try {
                val imageUrl = _uiState.value.selectedProfileImageUri?.let { uri ->
                    imgurRepository.uploadImage(uri)
                } ?: _uiState.value.user?.profileImageUrl

                val updatedUser = _uiState.value.user?.copy(
                    name = _uiState.value.displayName,
                    quote = _uiState.value.bio,
                    profileImageUrl = imageUrl
                )

                if (updatedUser != null) {
                    userRepository.saveUserProfile(updatedUser)
                    _uiState.update { it.copy(isSavingProfile = false, user = updatedUser, selectedProfileImageUri = null) }
                    _events.emit(ProfileUiEvent.ShowToast("Perfil salvo com sucesso!"))
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSavingProfile = false, error = e.localizedMessage) }
                _events.emit(ProfileUiEvent.ShowToast("Erro ao salvar perfil: ${e.localizedMessage}"))
            }
        }
    }

    fun resetProfile() {
        viewModelScope.launch {
            if (!_uiState.value.isMyProfile) return@launch

            val userId = authRepository.getCurrentUserId() ?: return@launch
            _uiState.update { it.copy(isLoading = true) }
            try {
                userRepository.getUserProfile(userId).first()?.let { user ->
                    _uiState.update { it.copy(
                        displayName = user.name,
                        bio = user.quote,
                        selectedProfileImageUri = null,
                        isLoading = false
                    )}
                    _events.emit(ProfileUiEvent.ShowToast("Campos resetados."))
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
            }
        }
    }
}