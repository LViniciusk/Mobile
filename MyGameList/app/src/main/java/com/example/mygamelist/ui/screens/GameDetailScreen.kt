package com.example.mygamelist.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.mygamelist.R
import com.example.mygamelist.viewmodel.GameDetailViewModel
import com.example.mygamelist.viewmodel.GameDetailUiEvent



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameDetailScreen(
    gameId: Int,
    onBack: () -> Unit,
    onNavigateToAddGameForm: (gameId: Int) -> Unit,
    viewModel: GameDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current

    LaunchedEffect(key1 = gameId) {
        if (gameId != -1) {
            viewModel.loadGameDetails(gameId)
        } else {
            viewModel._uiState.value = viewModel._uiState.value.copy(
                error = "ID do jogo inválido para detalhes."
            )
        }
    }

    LaunchedEffect(key1 = Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is GameDetailUiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is GameDetailUiEvent.NavigateToAddGameForm -> {
                    onNavigateToAddGameForm(event.gameId)
                }
                GameDetailUiEvent.NavigateBack -> {
                    onBack()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.gameDetail?.title ?: "Detalhes do Jogo", color = MaterialTheme.colorScheme.onSurface) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                )
            )
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Text(
                        text = "Carregando detalhes...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.offset(y = 40.dp)
                    )
                }
            }
            uiState.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Erro: ${uiState.error}",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadGameDetails(gameId) }) {
                            Text("Tentar Novamente")
                        }
                    }
                }
            }
            uiState.gameDetail == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Detalhes do jogo não encontrados.",
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            else -> {
                val game = uiState.gameDetail!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    ) {
                        AsyncImage(
                            model = game.imageUrl,
                            contentDescription = game.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                            placeholder = painterResource(id = R.drawable.ic_launcher_foreground),
                            error = painterResource(id = R.drawable.ic_launcher_foreground)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            MaterialTheme.colorScheme.background.copy(alpha = 0.7f),
                                            MaterialTheme.colorScheme.background.copy(alpha = 0.95f)
                                        ),
                                        startY = 0.4f * 300.dp.value
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(horizontal = 16.dp, vertical = 24.dp)
                        ) {
                            Text(
                                text = game.title,
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = "Rating",
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "${game.rating} / 5",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(Modifier.width(16.dp))
                                Text(
                                    text = "Metacritic: ${game.metacriticRating ?: "N/A"}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }

                    Column(modifier = Modifier.padding(16.dp)) {
                        Button(
                            onClick = { viewModel.toggleGameInUserList(game) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (uiState.isGameInUserList) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                contentColor = if (uiState.isGameInUserList) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text(if (uiState.isGameInUserList) "Remover da Minha Lista" else "Adicionar à Minha Lista")
                        }
                        Spacer(Modifier.height(16.dp))

                        Text(
                            text = "Descrição",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = game.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                        )
                        Spacer(Modifier.height(16.dp))

                        GameInfoSection(title = "Gêneros", content = game.genres)
                        GameInfoSection(title = "Plataformas", content = game.platforms)
                        GameInfoSection(title = "Desenvolvedoras", content = game.developers)
                        GameInfoSection(title = "Publicadoras", content = game.publishers)
                        GameInfoSection(title = "Tags", content = game.tags)

                        if (game.screenshots.isNotEmpty()) {
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = "Screenshots",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(Modifier.height(8.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(game.screenshots) { screenshotUrl ->
                                    AsyncImage(
                                        model = screenshotUrl,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .width(200.dp)
                                            .height(120.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop,
                                        placeholder = painterResource(id = R.drawable.ic_launcher_foreground),
                                        error = painterResource(id = R.drawable.ic_launcher_foreground)
                                    )
                                }
                            }
                        }

                        game.websiteUrl?.let { url ->
                            if (url.isNotBlank()) {
                                Spacer(Modifier.height(16.dp))
                                Button(onClick = { uriHandler.openUri(url) }) {
                                    Text("Visitar Site Oficial")
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun GameInfoSection(title: String, content: String?) {
    content?.let {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = it,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
        )
        Spacer(Modifier.height(12.dp))
    }
}