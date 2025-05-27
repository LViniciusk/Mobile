package com.example.mygamelist.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.mygamelist.R
import com.example.mygamelist.data.api.RetrofitClient
import com.example.mygamelist.data.model.GameResult
import kotlinx.coroutines.launch

@Composable
fun AddGameScreen(onBack: () -> Unit) {
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var results by remember { mutableStateOf<List<GameResult>>(emptyList()) }
    val gameCache = remember { mutableStateMapOf<String, List<GameResult>>() }
    val coroutineScope = rememberCoroutineScope()
    val apiKey = "84f487f7f7ce4190a0911d867db3c1ef"

    Column(
        modifier = Modifier
            .fillMaxSize()
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
                    .border(1.dp, Color.Gray, CircleShape)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Voltar",
                    tint = Color.Gray
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .clip(RoundedCornerShape(50))
                    .border(1.dp, Color.Gray, RoundedCornerShape(50))
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        val query = it.text.trim().lowercase()
                        if (query.length >= 3) {
                            if (gameCache.containsKey(query)) {
                                results = gameCache[query] ?: emptyList()
                            } else {
                                coroutineScope.launch {
                                    try {
                                        val response = RetrofitClient.api.searchGames(apiKey, query).results
                                        gameCache[query] = response
                                        results = response
                                    } catch (_: Exception) {
                                        results = emptyList()
                                    }
                                }
                            }
                        }
                    },
                    singleLine = true,
                    textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                    cursorBrush = SolidColor(Color.White),
                    modifier = Modifier.weight(1f),
                    decorationBox = { innerTextField ->
                        if (searchQuery.text.isEmpty()) {
                            Text(
                                text = "Pesquise por um jogo...",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                        innerTextField()
                    }
                )
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Pesquisar",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (searchQuery.text.isEmpty()) {
            PlaceholderSearch()
        } else {
            Text(
                text = "Resultados para \"${searchQuery.text}\"",
                color = Color.LightGray,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(results) { game ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = game.background_image,
                            contentDescription = null,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(game.name, color = Color.White, fontSize = 14.sp)
                            Text(
                                game.genres.joinToString { it.name },
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                            Text(game.released ?: "-", color = Color.DarkGray, fontSize = 12.sp)
                        }
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Adicionar",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
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
                contentDescription = null,
                modifier = Modifier.size(192.dp)
            )
        }
    }
}

