package com.example.mygamelist.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.example.mygamelist.data.model.User
import com.example.mygamelist.data.model.UserStats

data class ProfileUiState(val user: User)

class ProfileViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(
        ProfileUiState(
            user = User(
                id = "1",
                name = "LViniciusk",
                username = "LViniciusk",
                quote = "Cold is the void",
                stats = UserStats(all = 0, finished = 0, playing = 0, dropped = 0, want = 0),
                avatarUrl = 1
            )
        )
    )
    val uiState: StateFlow<ProfileUiState> = _uiState
}
