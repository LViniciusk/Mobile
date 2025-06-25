package com.example.postapp.ui


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.postapp.data.models.Post

@Composable
fun PostItem(post: Post, onDelete: (Int) -> Unit, onEdit: (Post) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        backgroundColor = Color(0xFFB3E5FC)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = post.title, style = MaterialTheme.typography.h6, color =
                Color(0xFF0D47A1))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = post.content, style = MaterialTheme.typography.body1, color =
                Color(0xFF01579B))
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                Button(
                    onClick = { showDialog = true },
                    colors = ButtonDefaults.buttonColors(backgroundColor =
                        Color(0xFFD32F2F)),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(text = "Deletar", color = Color.White)
                }
                Button(
                    onClick = { onEdit(post) },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF388E3C))
                ) {
                    Text(text = "Editar", color = Color.White)
                }
            }
        }
    }
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = "Confirmar Exclusão", color = Color(0xFFD32F2F)) },
            text = { Text(text = "Tem certeza que deseja excluir este post?") },
            confirmButton = {
                TextButton(onClick = { onDelete(post.id); showDialog = false }) {
                    Text(text = "Sim", color = Color(0xFFD32F2F))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(text = "Cancelar", color = Color.Gray)
                }
            }
        )
    }
}