package com.example.mygamelist.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.mygamelist.R
import com.example.mygamelist.viewmodel.ReviewGameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewGameScreen(
    onBack: () -> Unit,
    onNavigateToEdit: (gameId: Int) -> Unit,
    viewModel: ReviewGameViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (uiState.user != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = uiState.user!!.profileImageUrl,
                        contentDescription = "Foto de perfil",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape),
                        placeholder = painterResource(id = R.drawable.avatar_placeholder)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(uiState.user!!.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                        Text("@${uiState.user!!.username}", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar", tint = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        },
        bottomBar = {
            if (uiState.isMyReview) {
                BottomAppBar(
                    containerColor = Color.Transparent,
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    TextButton(onClick = { uiState.game?.id?.let { onNavigateToEdit(it) } }) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Editar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = uiState.error!!, color = MaterialTheme.colorScheme.error)
                }
            }
            uiState.game != null -> {
                val game = uiState.game!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(16.dp))

                    AsyncImage(
                        model = game.imageUrl,
                        contentDescription = "Poster do Jogo",
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .aspectRatio(0.75f)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(Modifier.height(24.dp))


                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${game.userRating?.toString() ?: "-"}/10",
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(Modifier.width(12.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = game.status.name,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = game.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(16.dp))

                    if (!game.userReview.isNullOrBlank()) {
                        Text(
                            text = game.userReview,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}