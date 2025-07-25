package com.example.mygamelist.ui.screens

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Button
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
import com.example.mygamelist.data.model.GameResult
import com.example.mygamelist.viewmodel.GameUiState
import com.example.mygamelist.viewmodel.GameViewModel
import com.example.mygamelist.viewmodel.GameUiEvent
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.material.icons.filled.Remove

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddGameScreen(
    onBack: () -> Unit,
    onNavigateToGameDetail: (gameId: Int) -> Unit,
    onNavigateToAddGameForm: (gameId: Int) -> Unit,
    viewModel: GameViewModel = hiltViewModel()
) {
    var searchQueryState by remember { mutableStateOf(TextFieldValue("")) }
    val gameUiState by viewModel.uiState
    val currentSearchQueryText by viewModel.searchQuery.collectAsState()

    val context = LocalContext.current

    LaunchedEffect(currentSearchQueryText) {
        if (searchQueryState.text != currentSearchQueryText) {
            searchQueryState = searchQueryState.copy(text = currentSearchQueryText)
        }
    }

    LaunchedEffect(key1 = Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is GameUiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Voltar",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Row(
                modifier = Modifier
                    .weight(1f)
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
                    decorationBox = { innerTextField ->
                        if (searchQueryState.text.isEmpty()) {
                            Text(
                                text = "Pesquise por um jogo...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Start
                            )
                        }
                        innerTextField()
                    }
                )
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Pesquisar",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (currentSearchQueryText.isNotEmpty()){
            Text(
                text = "Resultados para \"${currentSearchQueryText}\"",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            when (gameUiState) {
                is GameUiState.Idle -> {
                    if (currentSearchQueryText.length < 3) {
                        PlaceholderSearch()
                    }
                }
                is GameUiState.Loading -> {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
                is GameUiState.Success -> {
                    val games = (gameUiState as GameUiState.Success).games
                    val userSavedIds = (gameUiState as GameUiState.Success).userSavedGameIds
                    if (games.isEmpty()) {
                        Text(
                            text = "Nenhum resultado encontrado para \"${currentSearchQueryText}\"",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(games) { gameResult ->
                                val isAdded = userSavedIds.contains(gameResult.id)
                                GameResultItem(
                                    gameResult = gameResult,
                                    isAddedToUserList = isAdded,
                                    onToggleAddRemoveClick = { clickedGameResult, isCurrentlyAdded ->
                                        viewModel.toggleGameInUserList(clickedGameResult, isCurrentlyAdded)
                                        if (!isCurrentlyAdded) {
                                            onNavigateToAddGameForm(clickedGameResult.id)
                                        }
                                    },
                                    onCardClick = { clickedGameId -> onNavigateToGameDetail(clickedGameId) }
                                )
                            }
                        }
                    }
                }
                is GameUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = (gameUiState as GameUiState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.searchGames(currentSearchQueryText) }) {
                            Text("Tentar Novamente")
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun GameResultItem(
    gameResult: GameResult,
    isAddedToUserList: Boolean,
    onToggleAddRemoveClick: (GameResult, Boolean) -> Unit,
    onCardClick: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick(gameResult.id) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = gameResult.background_image,
            contentDescription = gameResult.name,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = R.drawable.ic_launcher_foreground),
            error = painterResource(id = R.drawable.ic_launcher_foreground)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(gameResult.name, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
            Text(
                gameResult.genres?.joinToString { it.name } ?: "Gênero Desconhecido",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                maxLines = 1
            )
            Text(gameResult.released ?: "-", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        }
        Icon(
            imageVector = if (isAddedToUserList) Icons.Filled.Remove else Icons.Filled.Add,
            contentDescription = if (isAddedToUserList) "Remover da lista" else "Adicionar à lista",
            tint = if (isAddedToUserList) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(24.dp)
                .clickable { onToggleAddRemoveClick(gameResult, isAddedToUserList) }
        )
    }
}

@Composable
private fun PlaceholderSearch() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.ic_search_placeholder),
                contentDescription = "Pesquisar por jogos",
                modifier = Modifier.size(192.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Comece a digitar para pesquisar por jogos",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}