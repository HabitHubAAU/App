package com.example.habithub.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ThemeMode {
    LIGHT,
    DARK
}

private val Context.themeDataStore by preferencesDataStore(name = "theme_preferences")

class ThemePreference(private val context: Context) {

    private val themeKey = stringPreferencesKey("theme_mode")

    val themeMode: Flow<ThemeMode> = context.themeDataStore.data.map { prefs ->
        when (prefs[themeKey]) {
            ThemeMode.DARK.name -> ThemeMode.DARK
            ThemeMode.LIGHT.name -> ThemeMode.LIGHT
            else -> ThemeMode.LIGHT
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.themeDataStore.edit { prefs ->
            prefs[themeKey] = mode.name
        }
    }
}
