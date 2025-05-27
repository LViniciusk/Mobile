package com.example.mygamelist.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.example.mygamelist.data.model.UserResult
import com.example.mygamelist.data.model.UsersList

@Composable
fun CommunityScreen() {
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    val localUsers = remember { UsersList }
    var filteredUsers by remember { mutableStateOf(emptyList<UserResult>()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
                        filteredUsers = if (searchQuery.text.isNotEmpty()) {
                            localUsers.filter { user ->
                                user.username.contains(searchQuery.text, ignoreCase = true) ||
                                        user.displayName.contains(searchQuery.text, ignoreCase = true)
                            }
                        } else {
                            emptyList()
                        }
                    },
                    singleLine = true,
                    textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                    cursorBrush = SolidColor(Color.White),
                    modifier = Modifier.weight(1f),
                    decorationBox = { innerTextField ->
                        if (searchQuery.text.isEmpty()) {
                            Text(
                                text = "Pesquise por um usuario...",
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
            UserList(UsersList)
        } else {
            Text(
                text = "Resultados para \"${searchQuery.text}\"",
                color = Color.LightGray,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            UserList(filteredUsers)
            if (filteredUsers.isEmpty()) PlaceholderSearch()
        }
    }
}

@Composable
private fun UserList(users: List<UserResult>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(users) { user ->
            UserListItem(user)
        }
    }
}

@Composable
private fun UserListItem(user: UserResult) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color.Gray.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            if (!user.avatarUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = user.avatarUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .border(1.dp, Color.DarkGray, CircleShape),
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.avatar_placeholder),
                    contentDescription = "Avatar",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            Text(
                text = user.displayName,
                color = Color.White,
                fontSize = 16.sp
            )
            Text(
                text = "@${user.username}",
                color = Color.Gray,
                fontSize = 12.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${user.gamesCount} jogos | ${user.followersCount} seguidores",
                color = Color.Gray,
                fontSize = 12.sp
            )
        }

        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Seguir",
            tint = Color.White,
            modifier = Modifier.size(24.dp)
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
                contentDescription = null,
                modifier = Modifier.size(192.dp)
            )
        }
    }
}