package com.example.mygamelist.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.mygamelist.R
import com.example.mygamelist.data.model.Game
import com.example.mygamelist.viewmodel.HomeViewModel
import com.example.mygamelist.viewmodel.HomeUiEvent
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.Remove
import com.example.mygamelist.viewmodel.AuthViewModel
import androidx.compose.material.icons.filled.Logout

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToSettings: (initialTabIndex: Int) -> Unit,
    onNavigateToGameDetail: (gameId: Int) -> Unit,
    onNavigateToAddGameForm: (gameId: Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(key1 = Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is HomeUiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is HomeUiEvent.NavigateToAddGameForm -> {
                    onNavigateToAddGameForm(event.gameId)
                }
            }
        }
    }


    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            AppDrawerContent(
                onNavigateToSettings = { tabIndex ->
                    scope.launch { drawerState.close() }
                    onNavigateToSettings(tabIndex)
                },
                onCloseDrawer = {
                    scope.launch { drawerState.close() }
                },
                authViewModel = authViewModel
            )
        }
    ) {
        Scaffold(
            topBar = {
                MyGameListTopAppBar(
                    onOpenDrawer = {
                        scope.launch { drawerState.open() }
                    }
                )
            },
            modifier = modifier
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .background(MaterialTheme.colorScheme.background)
            ) {
                WelcomeBanner(
                    modifier = Modifier.fillMaxWidth()
                )
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 20.dp)
                ) {
                    Text(
                        text = "Novos lançamentos",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.height(12.dp))

                    when {
                        uiState.newReleaseLoading -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                Text(
                                    text = "Carregando novos lançamentos...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.offset(y = 40.dp)
                                )
                            }
                        }
                        uiState.newReleaseError != null -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "Erro ao carregar novos lançamentos: ${uiState.newReleaseError}",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                Button(onClick = { viewModel.loadNewReleaseGames(forceRefresh = true) }) {
                                    Text("Tentar Novamente")
                                }
                            }
                        }
                        uiState.newReleaseGames.isEmpty() -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "Nenhum novo lançamento encontrado.",
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(Modifier.height(8.dp))
                                Button(onClick = { viewModel.loadNewReleaseGames(forceRefresh = true) }) {
                                    Text("Tentar Novamente")
                                }
                            }
                        }
                        else -> {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(uiState.newReleaseGames) { game ->
                                    val isAdded = uiState.userSavedGameIds.contains(game.id)
                                    ShowGameItem(
                                        game = game,
                                        isAddedToUserList = isAdded,
                                        onToggleAddRemoveClick = { clickedGame, _ ->
                                            viewModel.toggleGameInUserList(clickedGame)
                                        },
                                        onCardClick = { clickedGameId -> onNavigateToGameDetail(clickedGameId) }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    Text(
                        text = "Em Breve",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(Modifier.height(12.dp))

                    when {
                        uiState.comingSoonLoading -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                Text(
                                    text = "Carregando jogos em breve...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.offset(y = 40.dp)
                                )
                            }
                        }
                        uiState.comingSoonError != null -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "Erro ao carregar jogos em breve: ${uiState.comingSoonError}",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                Button(onClick = { viewModel.loadComingSoonGames(forceRefresh = true) }) {
                                    Text("Tentar Novamente")
                                }
                            }
                        }
                        uiState.comingSoonGames.isEmpty() -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "Nenhum jogo em breve encontrado.",
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(Modifier.height(8.dp))
                                Button(onClick = { viewModel.loadComingSoonGames(forceRefresh = true) }) {
                                    Text("Tentar Novamente")
                                }
                            }
                        }
                        else -> {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(uiState.comingSoonGames) { game ->
                                    val isAdded = uiState.userSavedGameIds.contains(game.id)
                                    ShowGameItem(
                                        game = game,
                                        isAddedToUserList = isAdded,
                                        onToggleAddRemoveClick = { clickedGame, _ ->
                                            viewModel.toggleGameInUserList(clickedGame)
                                        },
                                        onCardClick = { clickedGameId -> onNavigateToGameDetail(clickedGameId) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDrawerContent(
    onNavigateToSettings: (initialTabIndex: Int) -> Unit,
    onCloseDrawer: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    ModalDrawerSheet(
        modifier = Modifier.fillMaxWidth(0.7f),
        drawerContainerColor = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "Logo MyGameList",
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = "MyGameList",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Divider(color = MaterialTheme.colorScheme.outline)
        Spacer(Modifier.height(16.dp))
        NavigationDrawerItem(
            icon = { Icon(Icons.Filled.Settings, contentDescription = "Configurações", tint = MaterialTheme.colorScheme.onSurfaceVariant) },
            label = { Text("Configurações", color = MaterialTheme.colorScheme.onSurface) },
            selected = false,
            onClick = {
                onNavigateToSettings(1)
                scope.launch { onCloseDrawer() }
            },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            colors = NavigationDrawerItemDefaults.colors(
                unselectedContainerColor = MaterialTheme.colorScheme.surface,
                unselectedTextColor = MaterialTheme.colorScheme.onSurface,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        NavigationDrawerItem(
            icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Sair da conta", tint = MaterialTheme.colorScheme.onSurfaceVariant) },
            label = { Text("Sair da conta", color = MaterialTheme.colorScheme.onSurface) },
            selected = false,
            onClick = {
                scope.launch {
                    onCloseDrawer()
                    authViewModel.logout()
                }
            },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            colors = NavigationDrawerItemDefaults.colors(
                unselectedContainerColor = MaterialTheme.colorScheme.surface,
                unselectedTextColor = MaterialTheme.colorScheme.onSurface,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        NavigationDrawerItem(
            icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Fechar aplicativo", tint = MaterialTheme.colorScheme.onSurfaceVariant) },
            label = { Text("Fechar aplicativo", color = MaterialTheme.colorScheme.onSurface) },
            selected = false,
            onClick = {
                onCloseDrawer()
                val activity = (context as? Activity)
                activity?.finishAffinity()
            },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            colors = NavigationDrawerItemDefaults.colors(
                unselectedContainerColor = MaterialTheme.colorScheme.surface,
                unselectedTextColor = MaterialTheme.colorScheme.onSurface,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        Spacer(Modifier.height(16.dp))
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyGameListTopAppBar(
    onOpenDrawer: () -> Unit
) {
    TopAppBar(
        title = { Text("MyGameList", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface) },
        navigationIcon = {
            IconButton(onClick = onOpenDrawer) {
                Icon(
                    Icons.Filled.Menu,
                    contentDescription = "Abrir Menu",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        actions = { },
        colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}


@Composable
fun WelcomeBanner(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(400.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.backg),
            contentDescription = "Game background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Image(
            painter = painterResource(id = R.drawable.pet),
            contentDescription = "Pet",
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = 0.dp, y = 0.dp)
                .size(300.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            MaterialTheme.colorScheme.background.copy(alpha = 0.2f),
                            MaterialTheme.colorScheme.background.copy(alpha = 0.85f)
                        ),
                        startY = 150f
                    )
                )
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 20.dp, vertical = 32.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(Modifier.height(36.dp))
            Text(
                text = "Organize sua coleção de jogos e avaliações pessoais em um só lugar e compartilhe com o mundo",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.95f)
                ),
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.1
            )
        }
    }
}

@Composable
private fun ShowGameItem(
    game: Game,
    isAddedToUserList: Boolean,
    onToggleAddRemoveClick: (Game, Boolean) -> Unit,
    onCardClick: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(260.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onCardClick(game.id) },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .height(100.dp)
                    .fillMaxWidth()
            ) {
                AsyncImage(
                    model = game.imageUrl,
                    placeholder = painterResource(id = R.drawable.ic_launcher_foreground),
                    error = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = game.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                )

                Icon(
                    imageVector = if (isAddedToUserList) Icons.Filled.Remove else Icons.Filled.Add,
                    contentDescription = if (isAddedToUserList) "Remover da lista" else "Adicionar à lista",
                    tint = if (isAddedToUserList) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(32.dp)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), CircleShape)
                        .clickable { onToggleAddRemoveClick(game, isAddedToUserList) }
                        .padding(4.dp)
                )
            }

            Spacer(Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .padding(horizontal = 10.dp, vertical = 8.dp)
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Text(
                    text = game.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = game.genres ?: "Gênero Desconhecido",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = game.releaseYear ?: "Ano Desconhecido",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}