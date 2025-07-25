package com.example.mygamelist.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.mygamelist.R
import com.example.mygamelist.viewmodel.ProfileViewModel
import com.example.mygamelist.viewmodel.ProfileUiEvent
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.text.style.TextAlign



@Composable
fun ProfileScreen(
    onNavigateToSettings: (initialTabIndex: Int) -> Unit,
    onNavigateToFollowList: (userId: String, initialTab: Int) -> Unit,
    onNavigateToGameDetail: (gameId: Int) -> Unit,
    onNavigateToAddGameForm: (gameId: Int) -> Unit,
    onNavigateToReviewGame: (gameId: Int, userId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var searchQueryState by remember { mutableStateOf(TextFieldValue(uiState.searchQuery)) }

    LaunchedEffect(uiState.searchQuery) {
        if (searchQueryState.text != uiState.searchQuery) {
            searchQueryState = searchQueryState.copy(text = uiState.searchQuery)
        }
    }

    LaunchedEffect(key1 = Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ProfileUiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    if (uiState.isLoading || uiState.user == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            Text(
                text = "Carregando perfil...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.offset(y = 40.dp)
            )
        }
        return
    }

    uiState.error?.let { errorMessage ->
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Erro: $errorMessage",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
        return
    }

    val user = uiState.user!!
    val visibleGames = uiState.userGames
    val userStats = uiState.userStats

    var showProfileMenu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = user.profileImageUrl,
                    contentDescription = "Avatar do usuário",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.avatar_placeholder),
                    error = painterResource(id = R.drawable.avatar_placeholder)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(user.name, color = MaterialTheme.colorScheme.onBackground, fontSize = 20.sp)
                Text("@${user.username}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
            }
            if (uiState.isMyProfile) {
                Box {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "Opções do perfil",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { showProfileMenu = true }
                    )
                    DropdownMenu(
                        expanded = showProfileMenu,
                        onDismissRequest = { showProfileMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Editar perfil",
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            },
                            onClick = {
                                onNavigateToSettings(0)
                                showProfileMenu = false
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = "Editar perfil",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Baixar minha lista",
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            },
                            onClick = {
                                viewModel.onDownloadListClick()
                                showProfileMenu = false
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.FileDownload,
                                    contentDescription = "Baixar minha lista",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        )
                    }
                }
            }else {
                Button(
                    onClick = {
                        if (uiState.isFollowing) viewModel.unfollow() else viewModel.follow()
                    },
                    colors = if (uiState.isFollowing) ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ) else ButtonDefaults.buttonColors()
                ) {
                    Text(if (uiState.isFollowing) "Seguindo" else "Seguir")
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Row {
            Text(
                text = "${user.followersCount} Seguidores",
                modifier = Modifier.clickable { onNavigateToFollowList(user.id, 0) },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
            Spacer(Modifier.width(24.dp))
            Text(
                text = "${user.followingCount} Seguindo",
                modifier = Modifier.clickable { onNavigateToFollowList(user.id, 1) },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = user.quote,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                StatusChip(label = "Todos", count = userStats.all)
            }
            item {
                StatusChip(label = "Concluído", count = userStats.finished)
            }
            item {
                StatusChip(label = "Jogando", count = userStats.playing)
            }
            item {
                StatusChip(label = "Abandonado", count = userStats.dropped)
            }
            item {
                StatusChip(label = "Quero", count = userStats.wish)
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
                .clip(RoundedCornerShape(50))
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(50))
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = searchQueryState,
                onValueChange = { newValue ->
                    searchQueryState = newValue
                    viewModel.onSearchQueryChanged(newValue.text)
                },
                singleLine = true,
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Start
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier = Modifier.weight(1f),
                decorationBox = { inner ->
                    if (searchQueryState.text.isEmpty()) {
                        Text(
                            text = "Pesquisar na lista...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Start
                        )
                    }
                    inner()
                }
            )
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Pesquisar",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(visibleGames) { game ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToReviewGame(game.id, user.id) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = game.imageUrl,
                                contentDescription = game.title,
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop,
                                placeholder = painterResource(id = R.drawable.ic_launcher_foreground),
                                error = painterResource(id = R.drawable.ic_launcher_foreground)
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(MaterialTheme.colorScheme.primaryContainer)
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = game.userRating?.toString() ?: "0",
                                            color = MaterialTheme.colorScheme.tertiary,
                                            fontSize = 12.sp
                                        )
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(MaterialTheme.colorScheme.primaryContainer)
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = game.status.name,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                                Text(
                                    text = game.title,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 16.sp,
                                    modifier = Modifier.clickable { onNavigateToGameDetail(game.id) }
                                )
                                Text(
                                    game.genres ?: "Gênero Desconhecido",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                                Text(
                                    game.releaseYear ?: "Ano Desconhecido",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                            }
                            if (uiState.isMyProfile) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Editar Jogo",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clickable { onNavigateToAddGameForm(game.id) }
                                )
                            }
                        }

                        if (!game.userReview.isNullOrBlank()) {
                            Spacer(Modifier.height(12.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(
                                    model = user.profileImageUrl,
                                    contentDescription = "Sua foto de perfil",
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape),
                                    placeholder = painterResource(id = R.drawable.avatar_placeholder),
                                    error = painterResource(id = R.drawable.avatar_placeholder)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = game.userReview,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusChip(label: String, count: Int) {
    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = "$label: $count",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp
        )
    }
}