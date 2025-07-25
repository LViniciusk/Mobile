package com.example.mygamelist.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mygamelist.data.model.GameStatus
import com.example.mygamelist.viewmodel.AddGameFormViewModel
import com.example.mygamelist.viewmodel.AddGameFormUiEvent


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGameFormScreen(
    gameId: Int,
    onBack: () -> Unit,
    viewModel: AddGameFormViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var userReviewState by remember { mutableStateOf(TextFieldValue("")) }

    LaunchedEffect(uiState.userReview) {
        if (userReviewState.text != uiState.userReview.text) {
            userReviewState = uiState.userReview
        }
    }

    LaunchedEffect(key1 = Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is AddGameFormUiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                AddGameFormUiEvent.NavigateBack -> {
                    onBack()
                }
            }
        }
    }

    val isEditing = uiState.isGameInList
    val titleText = if (isEditing) "Editar Jogo" else "Adicionar Jogo"
    val buttonText = if (isEditing) "Salvar" else "Adicionar à Lista"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.game?.title ?: titleText, color = MaterialTheme.colorScheme.onSurface) },
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
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Text(
                        text = "Carregando detalhes do jogo...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.offset(y = 40.dp)
                    )
                }
                uiState.error != null -> {
                    Text(
                        text = "Erro: ${uiState.error}",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    Button(onClick = { viewModel.loadGameDetails(gameId) }) {
                        Text("Tentar Novamente")
                    }
                }
                uiState.game == null -> {
                    Text(
                        text = "Jogo não encontrado para formulário.",
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                else -> {

                    Text(
                        text = "Sua Avaliação (0-10): ${uiState.userRating ?: 0}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Slider(
                        value = (uiState.userRating ?: 0).toFloat(),
                        onValueChange = { newValue -> viewModel.onRatingChanged(newValue.toInt().coerceIn(0, 10)) },
                        valueRange = 0f..10f,
                        steps = 9,
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        )
                    )
                    Spacer(Modifier.height(24.dp))

                    Text(
                        text = "Status:",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GameStatus.entries.forEach { status ->
                            if (status != GameStatus.NONE) {
                                FilterChip(
                                    selected = uiState.selectedStatus == status,
                                    onClick = { viewModel.onStatusSelected(status) },
                                    label = { Text(text = status.name.replace("_", " ")) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        selectedBorderColor = MaterialTheme.colorScheme.primary,
                                        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                        selectedBorderWidth = 1.5.dp,
                                        borderWidth = 1.dp
                                    )
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(24.dp))

                    Text(
                        text = "Sua Análise:",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = userReviewState,
                        onValueChange = { newValue ->
                            userReviewState = newValue
                            viewModel.onReviewChanged(newValue)
                        },
                        label = { Text("") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            cursorColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        maxLines = 10,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences
                        )
                    )
                    Text(
                        text = "${userReviewState.text.length} / 5000 caracteres",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.End)
                    )
                    Spacer(Modifier.height(32.dp))

                    if (isEditing) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.removeUserGame() },
                                modifier = Modifier.weight(1f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                            ) {
                                Text("Remover", color = MaterialTheme.colorScheme.error)
                            }

                            Button(
                                onClick = { viewModel.saveUserGame() },
                                modifier = Modifier.weight(1f),
                                enabled = !uiState.isSaving,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                if (uiState.isSaving) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(buttonText, color = MaterialTheme.colorScheme.onPrimary)
                                }
                            }
                        }
                    } else {
                        Button(
                            onClick = { viewModel.saveUserGame() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isSaving,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            if (uiState.isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(buttonText, color = MaterialTheme.colorScheme.onPrimary)
                            }
                        }
                    }
                }
            }
        }
    }
}