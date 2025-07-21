package com.example.authapp2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.authapp2.model.repository.AuthRepository
import com.example.authapp2.ui.theme.AuthApp2Theme
import com.example.authapp2.ui.view.HomeScreen
import com.example.authapp2.ui.view.LoginScreen
import com.example.authapp2.ui.view.RecoveryScreen
import com.example.authapp2.ui.view.RegisterScreen
import com.example.authapp2.viewmodel.AuthViewModel
import com.example.authapp2.viewmodel.AuthViewModelFactory

class MainActivity : ComponentActivity() {

    sealed class UiScreenState {
        object Authenticated : UiScreenState()
        data class Unauthenticated(val currentScreen: String = "login") : UiScreenState()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val factory = AuthViewModelFactory(AuthRepository())
        val authViewModel: AuthViewModel by viewModels { factory }

        setContent {
            AuthApp2Theme {
                var currentUiScreenState by remember { mutableStateOf<UiScreenState>(UiScreenState.Unauthenticated()) }


                MainAppNavigation(
                    uiScreenState = currentUiScreenState,
                    viewModel = authViewModel,
                    onNavigateToRegister = {
                        currentUiScreenState = UiScreenState.Unauthenticated(currentScreen = "register")
                    },
                    onNavigateToLogin = {
                        currentUiScreenState = UiScreenState.Unauthenticated(currentScreen = "login")
                    },
                    onNavigateToRecovery = {
                        currentUiScreenState = UiScreenState.Unauthenticated(currentScreen = "recovery")
                    },
                    onUserAuthenticated = {
                        currentUiScreenState = UiScreenState.Authenticated
                    },
                    onUserLoggedOut = {
                        currentUiScreenState = UiScreenState.Unauthenticated(currentScreen = "login")
                    }
                )
            }
        }
    }
}

@Composable
fun MainAppNavigation(
    uiScreenState: MainActivity.UiScreenState,
    viewModel: AuthViewModel,
    onNavigateToRegister: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToRecovery: () -> Unit,
    onUserAuthenticated: () -> Unit,
    onUserLoggedOut: () -> Unit
) {
    when (uiScreenState) {
        is MainActivity.UiScreenState.Authenticated -> {
            HomeScreen(
                viewModel = viewModel,
                onLogout = {
                    viewModel.logout()
                    onUserLoggedOut()
                }
            )
        }
        is MainActivity.UiScreenState.Unauthenticated -> {
            when (uiScreenState.currentScreen) {
                "login" -> {
                    LoginScreen(
                        viewModel = viewModel,
                        onAuthenticated = onUserAuthenticated,
                        onNavigateToRegister = onNavigateToRegister,
                        onNavigateToRecovery = onNavigateToRecovery
                    )
                }
                "register" -> {
                    RegisterScreen(
                        viewModel = viewModel,
                        onNavigateToLogin = onNavigateToLogin,
                        onRegisterSuccess = onUserAuthenticated
                    )
                }
                "recovery" -> {
                    RecoveryScreen(
                        viewModel = viewModel,
                        onNavigateBack = onNavigateToLogin,
                    )
                }
            }
        }

    }
}

