package com.example.msgapp.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.msgapp.ui.theme.componentBackgroundColor
import com.example.msgapp.ui.theme.darkBackgroundColor
import com.example.msgapp.ui.theme.primaryAccentColor

@Composable
fun RoomSelector(onRoomSelected: (String) -> Unit) {
    var roomName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(darkBackgroundColor),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            color = componentBackgroundColor,
            tonalElevation = 4.dp,
            shape = MaterialTheme.shapes.large,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = roomName,
                    onValueChange = { roomName = it },
                    label = { Text("Nome da sala") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = primaryAccentColor,
                        focusedBorderColor = primaryAccentColor,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color.White.copy(alpha = 0.8f),
                        unfocusedLabelColor = Color.Gray,
                    )
                )

                Spacer(modifier = Modifier.width(12.dp))

                Button(
                    onClick = { if (roomName.isNotBlank()) onRoomSelected(roomName) },
                    shape = MaterialTheme.shapes.medium,
                    enabled = roomName.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryAccentColor,
                        contentColor = Color.White,
                        disabledContainerColor = Color.Gray.copy(alpha = 0.2f),
                        disabledContentColor = Color.Gray
                    )
                ) {
                    Text("Entrar")
                }
            }
        }
    }
}