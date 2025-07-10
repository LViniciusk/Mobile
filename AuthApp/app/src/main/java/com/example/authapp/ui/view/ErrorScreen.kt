package com.example.authapp.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.authapp.ui.theme.darkBackground
import com.example.authapp.ui.theme.darkGray
import com.example.authapp.ui.theme.darkOnBackground
import com.example.authapp.ui.theme.primaryColor

@Composable
fun ErrorScreen(
    message: String,
    onRetry: () -> Unit
) {


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "Ícone de Erro",
            modifier = Modifier.size(64.dp),
            tint = primaryColor
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Ocorreu um Erro",
            style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
            color = darkOnBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = darkGray
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = primaryColor,
                contentColor = Color.Black
            )
        ) {
            Text("Tentar Novamente")
        }
    }
}