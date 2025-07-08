package com.example.msgapp.ui.view

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.msgapp.model.Message
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.msgapp.ui.theme.componentBackgroundColor
import com.example.msgapp.ui.theme.darkBackgroundColor

@Composable
fun ChatScreen(
    username: String,
    userId: String,
    messages: List<Message>,
    onSend: (String) -> Unit,
    currentRoom: String,
    lastNotifiedId: String?,
    onNotify: (Message) -> Unit,
    onLeaveRoom: (() -> Unit)? = null
) {
    var input by remember { mutableStateOf("") }
    val context = LocalContext.current

    LaunchedEffect(messages.size) {
        val lastMsg = messages.lastOrNull()
        if (lastMsg != null && lastMsg.senderId != userId && lastMsg.id != lastNotifiedId) {
            onNotify(lastMsg)
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(darkBackgroundColor)
    ) {
        

        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Sala: $currentRoom",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.weight(1f))
            if (onLeaveRoom != null) {
                IconButton(onClick = { onLeaveRoom() }) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Trocar sala",
                        tint = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            reverseLayout = true
        ) {
            items(messages.reversed()) { msg ->
                MessageBubble(
                    msg = msg,
                    isOwn = msg.senderId == userId
                )
            }
        }

        Surface(
            color = componentBackgroundColor,
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 8.dp
        ) {
            Row(
                Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .navigationBarsPadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Digite sua mensagem...", color = Color.Gray) },
                    shape = RoundedCornerShape(24.dp),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        focusedContainerColor = Color(0xFF2C2C2C),
                        unfocusedContainerColor = Color(0xFF2C2C2C)
                    )
                )
                Spacer(Modifier.width(12.dp))
                IconButton(
                    onClick = {
                        if (input.isNotBlank()) {
                            onSend(input)
                            input = ""
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    enabled = input.isNotBlank()
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Enviar",
                        tint = Color.White
                    )
                }
            }
        }
    }
}


@Composable
fun MessageBubble(msg: Message, isOwn: Boolean) {
    val bubbleShape = RoundedCornerShape(20.dp)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isOwn) Arrangement.End else Arrangement.Start
    ) {
        if (isOwn) {
            val gradientBrush = Brush.horizontalGradient(
                colors = listOf(Color(0xFF8A2BE2), Color(0xFF4A00E0))
            )
            Box(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .clip(bubbleShape.copy(bottomEnd = CornerSize(4.dp)))
                    .background(gradientBrush)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                MessageContent(msg, Color.White)
            }
        } else {
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = msg.senderName,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                )
                Surface(
                    color = Color(0xFF2A2A2A),
                    shape = bubbleShape.copy(bottomStart = CornerSize(4.dp)),
                    modifier = Modifier.widthIn(max = 280.dp),
                    tonalElevation = 2.dp
                ) {
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                        MessageContent(msg, Color.White.copy(alpha = 0.9f))
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageContent(msg: Message, textColor: Color) {
    Column {
        Text(
            text = msg.text,
            style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 22.sp),
            color = textColor,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = android.text.format.DateFormat.format("HH:mm", msg.timestamp).toString(),
            style = MaterialTheme.typography.labelSmall,
            color = textColor.copy(alpha = 0.6f),
            modifier = Modifier.align(Alignment.End)
        )
    }
}

@RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
fun notifyNewMessage(context: Context, message: Message) {
    val channelId = "chat_messages"
    val notificationManager = NotificationManagerCompat.from(context)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            channelId, "Mensagens", NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)
    }
    val notification = NotificationCompat.Builder(context, channelId)
        .setContentTitle("Nova mensagem de ${message.senderName}")
        .setContentText(message.text)
        .setSmallIcon(android.R.drawable.ic_dialog_email)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .build()
    notificationManager.notify(message.id.hashCode(), notification)
}