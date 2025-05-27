package com.example.mygamelist.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.mygamelist.ui.theme.ThemePreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


private val Context.appSettingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "mygamelist_settings"
)

class SettingsRepository(private val context: Context) {

    private object PreferencesKeys {
        val THEME_PREFERENCE_KEY = stringPreferencesKey("theme_preference")
    }

    val themePreference: Flow<ThemePreference> = context.appSettingsDataStore.data
        .map { preferences ->
            ThemePreference.valueOf(
                preferences[PreferencesKeys.THEME_PREFERENCE_KEY] ?: ThemePreference.SYSTEM.name
            )
        }

    suspend fun setThemePreference(theme: ThemePreference) {
        context.appSettingsDataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_PREFERENCE_KEY] = theme.name
        }
    }
}