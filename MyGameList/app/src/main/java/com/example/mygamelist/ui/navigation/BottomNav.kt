package com.example.mygamelist.ui.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.compose.ui.graphics.ColorFilter
import androidx.navigation.navArgument
import com.example.mygamelist.R
import com.example.mygamelist.data.model.sampleGames
import com.example.mygamelist.data.model.sampleUser
import com.example.mygamelist.ui.screens.*
import com.example.mygamelist.viewmodel.ThemeViewModel

sealed class Screen(val route: String, val icon: Int) {
    object Home : Screen("home", R.drawable.ic_castle)
    object Community : Screen("community", R.drawable.ic_community)
    object Add : Screen("addgame", R.drawable.ic_add)
    object Notifications : Screen("notifications", R.drawable.ic_notifications)
    object Profile : Screen("profile", R.drawable.avatar_placeholder)
    object Settings : Screen("settings", R.drawable.avatar_placeholder)
}

@Composable
fun MyGameListNavHost(navController: NavHostController, padding: PaddingValues, themeViewModel : ThemeViewModel ) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = Modifier.padding(padding)
    ) {
        composable(Screen.Home.route) { HomeScreen(onNavigateToSettings = {tab -> navController.navigate("${Screen.Settings.route}/$tab") }) }
        composable(Screen.Community.route) { CommunityScreen() }
        composable(Screen.Add.route) {
            AddGameScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Notifications.route) { NotificationsScreen() }
        composable(Screen.Profile.route) { ProfileScreen(sampleUser, sampleGames, onNavigateToSettings = {tab -> navController.navigate("${Screen.Settings.route}/$tab") }) }
        composable(
            route = "${Screen.Settings.route}/{tab}",
            arguments = listOf(navArgument("tab") {
                type = NavType.IntType
                defaultValue = 0
            })
            ) {backStackEntry ->
                val initialTabIndex = backStackEntry.arguments?.getInt("tab") ?: 0
                SettingsScreen(tab = initialTabIndex, themeViewModel = themeViewModel)
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        Screen.Home,
        Screen.Community,
        Screen.Add,
        Screen.Notifications,
        Screen.Profile
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    if (currentRoute == Screen.Add.route || currentRoute?.startsWith(Screen.Settings.route) == true) {
        return
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { screen ->
            val isSelected = currentRoute == screen.route

            val iconColor = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }

            Image(
                painter = painterResource(id = screen.icon),
                contentDescription = null,
                modifier = Modifier
                    .size(28.dp)
                    .clickable {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                alignment = Alignment.Center,
                colorFilter = ColorFilter.tint(iconColor)
            )
        }
    }
}
