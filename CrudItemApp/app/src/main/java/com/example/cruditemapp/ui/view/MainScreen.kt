package com.example.cruditemapp.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cruditemapp.model.Item
import com.example.cruditemapp.ui.theme.noteColors
import com.example.cruditemapp.viewmodel.ItemViewModel
import kotlinx.coroutines.launch
import kotlin.random.Random




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: ItemViewModel = viewModel()) {
    val items by viewModel.items.collectAsState()
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<Item?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CrudItemApp :D") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                itemToEdit = null
                showBottomSheet = true
            }) {
                Icon(Icons.Filled.Add, contentDescription = "Adicionar Item")
            }
        }
    ) { paddingValues ->
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            modifier = Modifier.padding(paddingValues),
            contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalItemSpacing = 12.dp
        ) {
            items(items, key = { it.id }) { item ->
                ItemNote(
                    item = item,
                    onClick = {
                        itemToEdit = item
                        showBottomSheet = true
                    },
                    onDelete = { viewModel.deleteItem(item.id) }
                )
            }
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState
        ) {
            AddEditItemSheetContent(
                viewModel = viewModel,
                itemToEdit = itemToEdit,
                onSave = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            showBottomSheet = false
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun ItemNote(item: Item, onClick: () -> Unit, onDelete: () -> Unit) {
    val randomRotation = remember(item.id) { Random.nextFloat() * 5f - 2.5f }
    val randomColor = remember(item.id) { noteColors.random() }

    Box(
        modifier = Modifier
            .rotate(randomRotation)
            .clip(RoundedCornerShape(8.dp))
            .background(randomColor)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = item.title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.Black
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = item.description,
                fontSize = 14.sp,
                color = Color.DarkGray,
                lineHeight = 18.sp
            )
            Spacer(Modifier.height(8.dp))
            IconButton(
                onClick = onDelete,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Icon(Icons.Filled.Delete, contentDescription = "Deletar", tint = Color.Gray)
            }
        }
    }
}

@Composable
fun AddEditItemSheetContent(
    viewModel: ItemViewModel,
    itemToEdit: Item?,
    onSave: () -> Unit
) {
    LaunchedEffect(itemToEdit) {
        viewModel.titleState.value = itemToEdit?.title ?: ""
        viewModel.descriptionState.value = itemToEdit?.description ?: ""
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = if (itemToEdit == null) "Nova Nota" else "Editar Nota",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        OutlinedTextField(
            value = viewModel.titleState.value,
            onValueChange = { viewModel.titleState.value = it },
            label = { Text("Título") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = viewModel.descriptionState.value,
            onValueChange = { viewModel.descriptionState.value = it },
            label = { Text("Descrição") },
            modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp)
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                if (itemToEdit == null) {
                    viewModel.addItem()
                } else {
                    val updatedItem = itemToEdit.copy(
                        title = viewModel.titleState.value,
                        description = viewModel.descriptionState.value
                    )
                    viewModel.updateItem(updatedItem)
                }
                onSave()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Salvar")
        }
        Spacer(Modifier.height(16.dp))
    }
}