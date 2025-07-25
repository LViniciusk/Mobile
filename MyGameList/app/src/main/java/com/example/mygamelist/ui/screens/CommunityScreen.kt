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
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.mygamelist.R
import com.example.mygamelist.data.model.User
import com.example.mygamelist.viewmodel.CommunityUser
import com.example.mygamelist.viewmodel.CommunityViewModel

@Composable
fun CommunityScreen(
    onUserClicked: (String) -> Unit,
    viewModel: CommunityViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Membros MGL",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

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
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                singleLine = true,
                textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier = Modifier.weight(1f),
                decorationBox = { innerTextField ->
                    if (uiState.searchQuery.isEmpty()) {
                        Text(
                            text = "Pesquise por um usuário...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                    innerTextField()
                }
            )
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Pesquisar",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Box(modifier = Modifier.fillMaxSize()) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.error != null) {
                Text(
                    text = "Erro ao carregar usuários: ${uiState.error}",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (uiState.searchQuery.isNotEmpty() && uiState.displayedUsers.isEmpty()) {
                PlaceholderSearch()
            } else {
                UserList(
                    users = uiState.displayedUsers,
                    onUserClicked = onUserClicked,
                    onFollowClicked = { user -> viewModel.followUser(user) },
                    onUnfollowClicked = { user -> viewModel.unfollowUser(user) }
                )
            }
        }
    }
}

@Composable
private fun UserList(
    users: List<CommunityUser>,
    onUserClicked: (String) -> Unit,
    onFollowClicked: (User) -> Unit,
    onUnfollowClicked: (User) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        items(users) { communityUser ->
            UserListItem(
                communityUser = communityUser,
                onUserClicked = { onUserClicked(communityUser.user.id) },
                onFollowClicked = { onFollowClicked(communityUser.user) },
                onUnfollowClicked = { onUnfollowClicked(communityUser.user) }
            )
        }
    }
}

@Composable
internal fun UserListItem(
    communityUser: CommunityUser,
    onUserClicked: () -> Unit,
    onFollowClicked: () -> Unit,
    onUnfollowClicked: () -> Unit
) {
    val user = communityUser.user
    val isFollowing = communityUser.isFollowedByCurrentUser

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onUserClicked),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = user.profileImageUrl,
            contentDescription = "Avatar de ${user.name}",
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
            placeholder = painterResource(id = R.drawable.avatar_placeholder),
            error = painterResource(id = R.drawable.avatar_placeholder)
        )

        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            Text(
                text = user.name,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "@${user.username}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
        }

        IconButton(
            onClick = {
                if (isFollowing) onUnfollowClicked() else onFollowClicked()
            }
        ) {
            Icon(
                imageVector = if (isFollowing) Icons.Default.Remove else Icons.Default.Add,
                contentDescription = if (isFollowing) "Deixar de seguir ${user.name}" else "Seguir ${user.name}",
                tint = if (isFollowing) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun PlaceholderSearch() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_search_placeholder),
            contentDescription = "Nenhum usuário encontrado",
            modifier = Modifier.size(192.dp)
        )
    }
}