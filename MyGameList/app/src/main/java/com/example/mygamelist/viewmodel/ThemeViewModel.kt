package com.example.mygamelist.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mygamelist.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.example.mygamelist.ui.theme.ThemePreference
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class ThemeViewModel @Inject constructor( private val settingsRepository: SettingsRepository ) : ViewModel() {

    val themePreference: StateFlow<ThemePreference> = settingsRepository.themePreference
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = ThemePreference.SYSTEM
        )

    fun setThemePreference(theme: ThemePreference) {
        viewModelScope.launch {
            settingsRepository.setThemePreference(theme)
        }
    }
}