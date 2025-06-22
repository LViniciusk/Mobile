package com.example.mygamelist.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.mygamelist.R
import com.example.mygamelist.data.model.Game
import com.example.mygamelist.data.model.User

@Composable
fun ProfileScreen(
    user: User,
    userGames: List<Game>,
    onNavigateToSettings: (initialTabIndex: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var showProfileMenu by remember { mutableStateOf(false) }

    val visibleGames = remember(userGames, searchQuery) {
        if (searchQuery.text.isBlank()) userGames
        else userGames.filter {
            it.title.contains(searchQuery.text, ignoreCase = true)
        }
    }

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
                Image(
                    painter = painterResource(id = R.drawable.avatar_placeholder),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(user.name, color = MaterialTheme.colorScheme.onBackground, fontSize = 20.sp)
                Text("@${user.username}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
            }
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
                        text = { Text("Editar perfil", color = MaterialTheme.colorScheme.onSurface) },
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
                        text = { Text("Baixar minha lista", color = MaterialTheme.colorScheme.onSurface) },
                        onClick = {
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
        }

        Spacer(Modifier.height(8.dp))

        Row {
            Text("Seguindo ${user.stats.playing}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
            Spacer(Modifier.width(24.dp))
            Text("Seguidores ${user.stats.finished}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
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
                StatusChip(label = "Todos", count = user.stats.all)
            }
            item {
                StatusChip(label = "Concluído", count = user.stats.finished)
            }
            item {
                StatusChip(label = "Jogando", count = user.stats.playing)
            }
            item {
                StatusChip(label = "Abandonado", count = user.stats.dropped)
            }
            item {
                StatusChip(label = "Quero", count = user.stats.want)
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
                value = searchQuery,
                onValueChange = { searchQuery = it },
                singleLine = true,
                textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier = Modifier.weight(1f),
                decorationBox = { inner ->
                    if (searchQuery.text.isEmpty()) {
                        Text(
                            text = "Pesquisar na lista...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
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
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
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
                            Row (verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = game.metacriticRating?.toString() ?: "N/A",
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
                                        text = game.status.toString(),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            Text(game.title, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
                            Text(game.genres ?: "Gênero Desconhecido", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                            Text(game.releaseYear ?: "Ano Desconhecido", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        }
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Mais",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .size(20.dp)
                                .clickable { /* editar jogo */ }
                        )
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