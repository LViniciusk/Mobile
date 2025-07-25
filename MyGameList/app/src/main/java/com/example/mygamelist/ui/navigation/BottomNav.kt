package com.example.mygamelist.ui.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import com.example.mygamelist.R
import com.example.mygamelist.ui.screens.*
import com.example.mygamelist.viewmodel.ThemeViewModel

sealed class Screen(val route: String, val icon: Int) {
    object Login : Screen("login", 0)
    object Register : Screen("register", 0)
    object Home : Screen("home", R.drawable.ic_castle)
    object Community : Screen("community", R.drawable.ic_community)
    object Add : Screen("addgame", R.drawable.ic_add)
    object Notifications : Screen("notifications", R.drawable.ic_notifications)

    object FollowList : Screen("follow_list/{userId}/{initialTab}", 0) {
        fun buildRoute(userId: String, initialTab: Int) = "follow_list/$userId/$initialTab"
    }


    object Profile : Screen("profile?userId={userId}", R.drawable.avatar_placeholder) {
        fun withId(userId: String) = "profile?userId=$userId"
        fun myProfile() = "profile"
    }


    object ReviewGame : Screen("review_game/{gameId}/{userId}", 0) {
        fun withIds(gameId: Int, userId: String) = "review_game/$gameId/$userId"
    }

    object Settings : Screen("settings/{tab}", 0) {
        fun withTab(tab: Int) = "settings/$tab"
    }
    object GameDetail : Screen("game_detail/{gameId}", 0) {
        fun withId(id: Int) = "game_detail/$id"
    }
    object AddGameForm : Screen("add_game_form/{gameId}", 0) {
        fun withId(id: Int) = "add_game_form/$id"
    }
}

@Composable
fun MyGameListNavHost(
    navController: NavHostController,
    padding: PaddingValues,
    themeViewModel: ThemeViewModel,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.padding(padding)
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) }
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                onBack = { navController.popBackStack() },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToSettings = { tab -> navController.navigate(Screen.Settings.withTab(tab)) },
                onNavigateToGameDetail = { gameId -> navController.navigate(Screen.GameDetail.withId(gameId)) },
                onNavigateToAddGameForm = { gameId -> navController.navigate(Screen.AddGameForm.withId(gameId)) }
            )
        }
        composable(Screen.Community.route) {
            CommunityScreen(
                onUserClicked = { userId ->
                    navController.navigate(Screen.Profile.withId(userId))
                }
            )
        }

        composable(Screen.Add.route) {
            AddGameScreen(
                onBack = { navController.popBackStack() },
                onNavigateToGameDetail = { gameId -> navController.navigate(Screen.GameDetail.withId(gameId)) },
                onNavigateToAddGameForm = { gameId -> navController.navigate(Screen.AddGameForm.withId(gameId)) }
            )
        }
        composable(Screen.Notifications.route) {
            NotificationsScreen(
                onUserClicked = { userId ->
                    navController.navigate(Screen.Profile.withId(userId))
                }
            )
        }

        composable(
            route = Screen.FollowList.route,
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType },
                navArgument("initialTab") { type = NavType.IntType }
            )
        ) {
            FollowListScreen(
                onBack = { navController.popBackStack() },
                onUserClicked = { userId -> navController.navigate(Screen.Profile.withId(userId)) }
            )
        }

        composable(
            route = Screen.Profile.route,
            arguments = listOf(
                navArgument("userId") {
                    type = NavType.StringType
                    nullable = true
                }
            )
        ) {
            ProfileScreen(
                onNavigateToSettings = { tab -> navController.navigate(Screen.Settings.withTab(tab)) },
                onNavigateToGameDetail = { gameId -> navController.navigate(Screen.GameDetail.withId(gameId)) },
                onNavigateToAddGameForm = { gameId -> navController.navigate(Screen.AddGameForm.withId(gameId)) },
                onNavigateToReviewGame = { gameId, userId -> navController.navigate(Screen.ReviewGame.withIds(gameId, userId)) },
                onNavigateToFollowList = { userId, initialTab -> navController.navigate(Screen.FollowList.buildRoute(userId, initialTab)) }
            )
        }

        composable(
            route = Screen.Settings.route,
            arguments = listOf(navArgument("tab") { type = NavType.IntType; defaultValue = 0 })
        ) { backStackEntry ->
            val tab = backStackEntry.arguments?.getInt("tab") ?: 0
            SettingsScreen(tab = tab, themeViewModel = themeViewModel)
        }

        composable(
            route = Screen.GameDetail.route,
            arguments = listOf(navArgument("gameId") { type = NavType.IntType })
        ) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getInt("gameId") ?: -1
            GameDetailScreen(
                gameId = gameId,
                onBack = { navController.popBackStack() },
                onNavigateToAddGameForm = { id -> navController.navigate(Screen.AddGameForm.withId(id)) }
            )
        }

        composable(
            route = Screen.AddGameForm.route,
            arguments = listOf(navArgument("gameId") { type = NavType.IntType })
        ) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getInt("gameId") ?: -1
            AddGameFormScreen(
                gameId = gameId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.ReviewGame.route,
            arguments = listOf(
                navArgument("gameId") { type = NavType.IntType },
                navArgument("userId") { type = NavType.StringType }
            )
        ) {
            ReviewGameScreen(
                onBack = { navController.popBackStack() },
                onNavigateToEdit = { gameId ->
                    navController.navigate(Screen.AddGameForm.withId(gameId))
                }
            )
        }
    }
}

@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    profileImageUrl: String?
) {
    val items = listOf(
        Screen.Home,
        Screen.Community,
        Screen.Add,
        Screen.Notifications,
        Screen.Profile
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val noBottomBarRoutes = listOf(
        Screen.Login.route,
        Screen.Register.route,
        Screen.Add.route
    )

    if (currentRoute in noBottomBarRoutes ||
        currentRoute?.startsWith("settings") == true ||
        currentRoute?.startsWith("game_detail") == true ||
        currentRoute?.startsWith("add_game_form") == true ||
        currentRoute?.startsWith("review_game") == true
    ) {
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
            val iconColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant

            if (screen == Screen.Profile) {
                AsyncImage(
                    model = profileImageUrl,
                    contentDescription = "Perfil",
                    placeholder = painterResource(id = R.drawable.avatar_placeholder),
                    error = painterResource(id = R.drawable.avatar_placeholder),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .border(
                            width = if (isSelected) 2.dp else 0.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = CircleShape
                        )
                        .clickable { navigateToScreen(navController, Screen.Profile.myProfile()) }
                )
            } else {
                Image(
                    painter = painterResource(id = screen.icon),
                    contentDescription = null,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { navigateToScreen(navController, screen.route) },
                    alignment = Alignment.Center,
                    colorFilter = ColorFilter.tint(iconColor)
                )
            }
        }
    }
}

private fun navigateToScreen(navController: NavHostController, route: String) {
    navController.navigate(route) {
        popUpTo(navController.graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}