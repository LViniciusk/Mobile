package com.example.authapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.authapp.remote.RetrofitClient
import com.example.authapp.repository.AuthRepository
import com.example.authapp.ui.theme.AuthAppTheme
import com.example.authapp.ui.view.ErrorScreen
import com.example.authapp.ui.view.HomeScreen
import com.example.authapp.ui.view.LoginScreen
import com.example.authapp.ui.view.RegisterScreen 
import com.example.authapp.viewmodel.AuthState
import com.example.authapp.viewmodel.AuthViewModel
import com.example.authapp.viewmodel.AuthViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val factory = AuthViewModelFactory(
            AuthRepository(
                api = RetrofitClient.api,
                context = applicationContext
            )
        )
        val authViewModel: AuthViewModel by viewModels { factory }

        authViewModel.checkAuth()

        setContent {
            AuthAppTheme {
                val authState by authViewModel.authState.collectAsState()
                
                var authScreenToShow by remember { mutableStateOf("login") }

                when (authState) {
                    is AuthState.Authenticated -> {
                        HomeScreen(
                            viewModel = authViewModel,
                            onLogout = { authViewModel.logout() }
                        )
                    }

                    is AuthState.Unauthenticated, is AuthState.Idle -> {

                        when (authScreenToShow) {
                            "login" -> {
                                LoginScreen(
                                    viewModel = authViewModel,
                                    onAuthenticated = { },
                                    onNavigateToRegister = { authScreenToShow = "register" }
                                )
                            }
                            "register" -> {
                                RegisterScreen(
                                    viewModel = authViewModel,
                                    onNavigateToLogin = { authScreenToShow = "login" },
                                    onRegisterSuccess = { }
                                )
                            }
                        }
                    }

                    is AuthState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    is AuthState.Error -> {
                        val message = (authState as AuthState.Error).message
                        ErrorScreen(
                            message = message,
                            onRetry = { authViewModel.checkAuth() }
                        )
                    }
                }
            }
        }
    }
}