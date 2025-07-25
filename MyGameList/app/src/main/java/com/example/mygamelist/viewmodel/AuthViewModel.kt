package com.example.mygamelist.viewmodel

import android.app.Activity
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mygamelist.data.model.User
import com.example.mygamelist.data.repository.AuthRepository
import com.example.mygamelist.data.repository.GameRepository
import com.example.mygamelist.data.repository.ImgurRepository
import com.example.mygamelist.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update

data class AuthScreenState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val username: String = "",
    val profileImageUri: Uri? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAuthenticated: Boolean = false,
    val currentUserProfile: User? = null,

    val currentUserEmail: String = "",
    val isEmailVerified: Boolean = false,
    val currentPasswordInput: String = "",
    val newPasswordInput: String = "",
    val confirmNewPasswordInput: String = "",
    val isChangingPassword: Boolean = false,
    val changePasswordError: String? = null,

    val isDeletingAccount: Boolean = false,
    val deleteAccountError: String? = null
)

sealed class AuthUiEvent {
    data class ShowToast(val message: String) : AuthUiEvent()
    object NavigateToHome : AuthUiEvent()
    object NavigateToLogin : AuthUiEvent()
    object NavigateToRegister : AuthUiEvent()
    object ReauthenticateRequired : AuthUiEvent()
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val gameRepository: GameRepository,
    private val imgurRepository: ImgurRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthScreenState())
    val uiState: StateFlow<AuthScreenState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AuthUiEvent>()
    val events: SharedFlow<AuthUiEvent> = _events.asSharedFlow()

    init {
        authRepository.getCurrentUser()
            .flatMapLatest { firebaseUser ->
                if (firebaseUser != null) {
                    userRepository.getUserProfile(firebaseUser.uid)
                } else {
                    flowOf(null)
                }
            }
            .onEach { userProfile ->
                val firebaseUser = authRepository.getCurrentUser().first()
                _uiState.update { it.copy(
                    isAuthenticated = firebaseUser != null,
                    currentUserEmail = firebaseUser?.email ?: "",
                    isEmailVerified = firebaseUser?.isEmailVerified == true,
                    currentUserProfile = userProfile
                )}
            }
            .launchIn(viewModelScope)
    }

    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(email = email, error = null)
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(password = password, error = null)
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = confirmPassword, error = null)
    }

    fun onUsernameChange(username: String) {
        _uiState.value = _uiState.value.copy(username = username, error = null)
    }

    fun onProfileImageSelected(uri: Uri?) {
        _uiState.value = _uiState.value.copy(profileImageUri = uri, error = null)
    }

    fun onCurrentPasswordInputChange(password: String) {
        _uiState.value = _uiState.value.copy(currentPasswordInput = password, changePasswordError = null)
    }

    fun onNewPasswordInputChange(password: String) {
        _uiState.value = _uiState.value.copy(newPasswordInput = password, changePasswordError = null)
    }

    fun onConfirmNewPasswordInputChange(password: String) {
        _uiState.value = _uiState.value.copy(confirmNewPasswordInput = password, changePasswordError = null)
    }

    fun signInWithGoogleToken(idToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val userId = authRepository.signInWithGoogle(idToken)
                gameRepository.syncUserGamesFromFirestoreToRoom(userId)
                _uiState.update { it.copy(isLoading = false) }
                _events.emit(AuthUiEvent.NavigateToHome)
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
                _events.emit(AuthUiEvent.ShowToast(e.localizedMessage ?: "Erro desconhecido"))
            }
        }
    }

    fun register() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            if (_uiState.value.password != _uiState.value.confirmPassword) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "As senhas não coincidem.")
                _events.emit(AuthUiEvent.ShowToast("As senhas não coincidem."))
                return@launch
            }
            if (_uiState.value.username.isBlank()) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "O nome de usuário não pode estar vazio.")
                _events.emit(AuthUiEvent.ShowToast("O nome de usuário não pode estar vazio."))
                return@launch
            }

            try {
                val imageUrl = _uiState.value.profileImageUri?.let { uri ->
                    Log.d("AuthViewModel", "Iniciando upload da imagem antes do registro.")

                    imgurRepository.uploadImage(uri)
                } ?: "https://placehold.co/120x120/E0E0E0/000000?text=${_uiState.value.username.firstOrNull()?.uppercase() ?: "U"}"

                Log.d("AuthViewModel", "Upload concluído. URL da imagem: $imageUrl")

                val userId = authRepository.register(_uiState.value.email, _uiState.value.password)

                Log.d("AuthViewModel", "Usuário registrado no Firebase Auth com UID: $userId")

                userRepository.createInitialProfile(
                    userId = userId,
                    username = _uiState.value.username,
                    email = _uiState.value.email,
                    profileImageUrl = imageUrl
                )
                Log.d("AuthViewModel", "Perfil do usuário criado no Firestore.")
                gameRepository.syncUserGamesFromFirestoreToRoom(userId)

                _uiState.value = _uiState.value.copy(isLoading = false, isAuthenticated = true)
                _events.emit(AuthUiEvent.ShowToast("Registro bem-sucedido!"))
                _events.emit(AuthUiEvent.NavigateToHome)

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.localizedMessage ?: "Erro no registro.")
                _events.emit(AuthUiEvent.ShowToast("Erro no registro: ${e.localizedMessage}"))
                Log.e("AuthViewModel", "Erro no registro: ${e.localizedMessage}", e)
            }
        }
    }

    fun login() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val userId = authRepository.login(_uiState.value.email, _uiState.value.password)
                gameRepository.syncUserGamesFromFirestoreToRoom(userId)

                _uiState.value = _uiState.value.copy(isLoading = false, isAuthenticated = true)
                _events.emit(AuthUiEvent.ShowToast("Login bem-sucedido!"))
                _events.emit(AuthUiEvent.NavigateToHome)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.localizedMessage ?: "Erro no login."
                )
                _events.emit(AuthUiEvent.ShowToast("Erro no login: ${e.localizedMessage}"))
                Log.e("AuthViewModel", "Erro no login: ${e.localizedMessage}", e)
            }
        }
    }

    fun logout() {
        authRepository.logout()
        _uiState.value = _uiState.value.copy(isAuthenticated = false)
        viewModelScope.launch {
            _events.emit(AuthUiEvent.ShowToast("Você foi desconectado."))
            _events.emit(AuthUiEvent.NavigateToLogin)
        }
    }

    fun changePassword() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isChangingPassword = true, changePasswordError = null)
            val currentUser = authRepository.getCurrentUser().first()
            val currentPassword = _uiState.value.currentPasswordInput
            val newPassword = _uiState.value.newPasswordInput
            val confirmNewPassword = _uiState.value.confirmNewPasswordInput

            if (currentUser == null) {
                _uiState.value = _uiState.value.copy(isChangingPassword = false, changePasswordError = "Usuário não logado.")
                _events.emit(AuthUiEvent.ShowToast("Erro: Usuário não logado."))
                return@launch
            }
            if (newPassword.isBlank() || newPassword.length < 6) {
                _uiState.value = _uiState.value.copy(isChangingPassword = false, changePasswordError = "A nova senha deve ter pelo menos 6 caracteres.")
                _events.emit(AuthUiEvent.ShowToast("A nova senha deve ter pelo menos 6 caracteres."))
                return@launch
            }
            if (newPassword != confirmNewPassword) {
                _uiState.value = _uiState.value.copy(isChangingPassword = false, changePasswordError = "As novas senhas não coincidem.")
                _events.emit(AuthUiEvent.ShowToast("As novas senhas não coincidem."))
                return@launch
            }

            try {
                val email = currentUser.email
                if (email == null) {
                    _uiState.value = _uiState.value.copy(isChangingPassword = false, changePasswordError = "E-mail do usuário não encontrado.")
                    _events.emit(AuthUiEvent.ShowToast("Erro: E-mail do usuário não encontrado."))
                    return@launch
                }
                val credential = EmailAuthProvider.getCredential(email, currentPassword)
                currentUser.reauthenticate(credential).await()

                currentUser.updatePassword(newPassword).await()
                _uiState.value = _uiState.value.copy(
                    isChangingPassword = false,
                    currentPasswordInput = "",
                    newPasswordInput = "",
                    confirmNewPasswordInput = ""
                )
                _events.emit(AuthUiEvent.ShowToast("Senha alterada com sucesso!"))
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isChangingPassword = false)
                when (e) {
                    is FirebaseAuthRecentLoginRequiredException -> {
                        _uiState.value = _uiState.value.copy(changePasswordError = "Por favor, faça login novamente para alterar a senha.")
                        _events.emit(AuthUiEvent.ShowToast("Sessão expirada. Faça login novamente."))
                        _events.emit(AuthUiEvent.ReauthenticateRequired)
                    }
                    is FirebaseAuthInvalidCredentialsException -> {
                        _uiState.value = _uiState.value.copy(changePasswordError = "Senha atual incorreta.")
                        _events.emit(AuthUiEvent.ShowToast("Senha atual incorreta."))
                    }
                    else -> {
                        _uiState.value = _uiState.value.copy(changePasswordError = e.localizedMessage ?: "Erro ao alterar senha.")
                        _events.emit(AuthUiEvent.ShowToast("Erro ao alterar senha: ${e.localizedMessage}"))
                    }
                }
                Log.e("AuthViewModel", "Erro ao alterar senha: ${e.localizedMessage}", e)
            }
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeletingAccount = true, deleteAccountError = null)
            val currentUser = authRepository.getCurrentUser().first()
            val currentPassword = _uiState.value.currentPasswordInput

            if (currentUser == null) {
                _uiState.value = _uiState.value.copy(isDeletingAccount = false, deleteAccountError = "Usuário não logado.")
                _events.emit(AuthUiEvent.ShowToast("Erro: Usuário não logado."))
                return@launch
            }

            try {
                val email = currentUser.email
                if (email == null) {
                    _uiState.value = _uiState.value.copy(isDeletingAccount = false, deleteAccountError = "E-mail do usuário não encontrado.")
                    _events.emit(AuthUiEvent.ShowToast("Erro: E-mail do usuário não encontrado."))
                    return@launch
                }
                val credential = EmailAuthProvider.getCredential(email.toString(), currentPassword)
                currentUser.reauthenticate(credential).await()

                val userId = currentUser.uid
                gameRepository.deleteUserGamesFromFirestore(userId)
                gameRepository.deleteUserGames(userId)

                currentUser.delete().await()

                _uiState.value = _uiState.value.copy(isDeletingAccount = false, isAuthenticated = false)
                _events.emit(AuthUiEvent.ShowToast("Conta excluída com sucesso!"))
                _events.emit(AuthUiEvent.NavigateToLogin)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isDeletingAccount = false)
                when (e) {
                    is FirebaseAuthRecentLoginRequiredException -> {
                        _uiState.value = _uiState.value.copy(deleteAccountError = "Por favor, faça login novamente para excluir a conta.")
                        _events.emit(AuthUiEvent.ShowToast("Sessão expirada. Faça login novamente."))
                        _events.emit(AuthUiEvent.ReauthenticateRequired)
                    }
                    is FirebaseAuthInvalidCredentialsException -> {
                        _uiState.value = _uiState.value.copy(deleteAccountError = "Senha incorreta para exclusão de conta.")
                        _events.emit(AuthUiEvent.ShowToast("Senha incorreta."))
                    }
                    else -> {
                        _uiState.value = _uiState.value.copy(deleteAccountError = e.localizedMessage ?: "Erro ao excluir conta.")
                        _events.emit(AuthUiEvent.ShowToast("Erro ao excluir conta: Campo de senha vazio"))
                    }
                }
                Log.e("AuthViewModel", "Erro ao excluir conta: ${e.localizedMessage}", e)
            }
        }
    }
}