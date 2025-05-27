package com.example.mygamelist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.Scaffold
import androidx.navigation.compose.rememberNavController
import com.example.mygamelist.data.repository.SettingsRepository
import com.example.mygamelist.ui.navigation.BottomNavigationBar
import com.example.mygamelist.ui.navigation.MyGameListNavHost
import com.example.mygamelist.ui.theme.MyGameListTheme
import com.example.mygamelist.viewmodel.ThemeViewModel
import com.example.mygamelist.viewmodel.ThemeViewModelFactory

class MainActivity : ComponentActivity() {

    private val settingsRepository by lazy {
        SettingsRepository(applicationContext)
    }

    private val themeViewModel: ThemeViewModel by viewModels {
        ThemeViewModelFactory(settingsRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MyGameListTheme(themeViewModel = themeViewModel) {
                val navController = rememberNavController()
                Scaffold(
                    bottomBar = {
                        BottomNavigationBar(navController = navController)
                    }
                ) { innerPadding ->
                    MyGameListNavHost(
                        navController = navController,
                        padding = innerPadding,
                        themeViewModel = themeViewModel
                    )
                }
            }
        }
    }
}

