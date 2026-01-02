package pt.isec.a2022143267.safetysec.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// DataStore extension
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * Theme Manager for handling light/dark mode preferences
 */
class ThemePreferences(private val context: Context) {

    companion object {
        private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
    }

    /**
     * Get the current dark mode preference as a Flow
     */
    val isDarkMode: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[DARK_MODE_KEY] ?: false // Default to light mode
    }

    /**
     * Toggle dark mode on/off
     */
    suspend fun toggleDarkMode() {
        context.dataStore.edit { preferences ->
            val currentMode = preferences[DARK_MODE_KEY] ?: false
            preferences[DARK_MODE_KEY] = !currentMode
        }
    }

    /**
     * Set dark mode explicitly
     */
    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = enabled
        }
    }
}

