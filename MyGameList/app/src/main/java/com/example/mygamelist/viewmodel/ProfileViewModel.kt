package com.example.mygamelist.viewmodel

import androidx.lifecycle.ViewModel
import com.example.mygamelist.data.model.User

data class ProfileUiState(val user: User)

class ProfileViewModel : ViewModel() { }
