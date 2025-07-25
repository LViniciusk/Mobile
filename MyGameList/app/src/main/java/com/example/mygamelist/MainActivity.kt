package com.example.mygamelist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.example.mygamelist.ui.navigation.BottomNavigationBar
import com.example.mygamelist.ui.navigation.MyGameListNavHost
import com.example.mygamelist.ui.navigation.Screen
import com.example.mygamelist.ui.screens.SplashScreen
import com.example.mygamelist.ui.theme.MyGameListTheme
import com.example.mygamelist.viewmodel.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val themeViewModel: ThemeViewModel = hiltViewModel()
            MyGameListTheme(themeViewModel = themeViewModel) {
                val authState by mainViewModel.authState.collectAsState()

                when (authState) {
                    AuthVerificationState.Loading -> {
                        SplashScreen()
                    }
                    AuthVerificationState.Authenticated -> {
                        AppContent(
                            themeViewModel = themeViewModel,
                            authViewModel = authViewModel,
                            startDestination = Screen.Home.route
                        )
                    }
                    AuthVerificationState.Unauthenticated -> {
                        AppContent(
                            themeViewModel = themeViewModel,
                            authViewModel = authViewModel,
                            startDestination = Screen.Login.route
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppContent(
    themeViewModel: ThemeViewModel,
    startDestination: String,
    authViewModel: AuthViewModel
) {
    val navController = rememberNavController()
    val authState by authViewModel.uiState.collectAsState()

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                profileImageUrl = authState.currentUserProfile?.profileImageUrl
            )
        }
    ) { innerPadding ->
        MyGameListNavHost(
            navController = navController,
            padding = innerPadding,
            themeViewModel = themeViewModel,
            startDestination = startDestination
        )
    }
}