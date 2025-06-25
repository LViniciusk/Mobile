package com.example.mygamelist.ui.screens

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.mygamelist.R
import com.example.mygamelist.data.model.GameResult
import com.example.mygamelist.viewmodel.GameUiEvent
import com.example.mygamelist.viewmodel.GameUiState
import com.example.mygamelist.viewmodel.GameViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddGameScreen(
    onBack: () -> Unit,
    viewModel: GameViewModel = hiltViewModel() // Assumindo que GameViewModel também é HiltViewModel
) {
    // Mantém o TextFieldValue no estado local do Composable para controle de cursor/seleção
    var searchQueryState by remember { mutableStateOf(TextFieldValue("")) }

    // Observa o GameUiState da ViewModel (usando .value diretamente para State<T>)
    val gameUiState = viewModel.uiState.value

    // Coleta o StateFlow _searchQuery da ViewModel para exibir o texto da busca
    val currentSearchQueryText by viewModel._searchQuery.collectAsState()

    val context = LocalContext.current // Obtém o Context para o Toast

    // Sincroniza o texto do TextFieldValue local com a query da ViewModel
    // Garante que o TextField reflete o que a ViewModel tem, sem perder o cursor.
    LaunchedEffect(currentSearchQueryText) {
        if (searchQueryState.text != currentSearchQueryText) {
            searchQueryState = searchQueryState.copy(text = currentSearchQueryText)
        }
    }

    // LaunchedEffect para observar os eventos da GameViewModel (como ShowToast)
    LaunchedEffect(key1 = Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is GameUiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                // Adicione outros eventos GameUiEvent aqui
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) // Usar cor de fundo do tema
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
                    value = searchQueryState, // Usa o estado local do TextFieldValue
                    onValueChange = { newValue ->
                        searchQueryState = newValue
                        viewModel.onSearchQueryChanged(newValue.text) // Passa apenas o texto para a ViewModel
                    },
                    singleLine = true,
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Start // Alinhamento à esquerda
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier.weight(1f),
                    decorationBox = { innerTextField ->
                        // Usa o texto do estado local para verificar se está vazia
                        if (searchQueryState.text.isEmpty()) {
                            Text(
                                text = "Pesquise por um jogo...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Start // Alinhamento à esquerda para placeholder
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

        // Exibe "Resultados para..." apenas se houver uma query de busca (currentSearchQueryText)
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
            when (gameUiState) { // Usa gameUiState (o State<GameUiState> da ViewModel)
                is GameUiState.Idle -> {
                    // Exibe o placeholder se a query for menor que 3 e não houver resultados
                    if (currentSearchQueryText.length < 3) {
                        PlaceholderSearch()
                    }
                    // Se a query for >=3 mas ainda Idle (ex: debounce esperando), não mostra nada ou um placeholder diferente
                }
                is GameUiState.Loading -> {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
                is GameUiState.Success -> {
                    val games = gameUiState.games // Acessa a lista de jogos do GameUiState.Success
                    if (games.isEmpty()) {
                        Text(
                            text = "Nenhum resultado encontrado para \"${currentSearchQueryText}\"",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(games) { gameResult ->
                                GameResultItem(gameResult) { clickedGameResult ->
                                    viewModel.addGameToUserGames(clickedGameResult)
                                }
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
                            text = gameUiState.message, // Acessa a mensagem do GameUiState.Error
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.searchGames(currentSearchQueryText) }) { // Adiciona o botão de tentar novamente
                            Text("Tentar Novamente")
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun GameResultItem(gameResult: GameResult, onAddClick: (GameResult) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
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
            imageVector = Icons.Default.Add,
            contentDescription = "Adicionar",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(24.dp)
                .clickable { onAddClick(gameResult) }
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