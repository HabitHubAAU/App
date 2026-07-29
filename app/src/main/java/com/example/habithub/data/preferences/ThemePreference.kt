package com.example.habithub.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repräsentiert die verfügbaren Design-Modi der App.
 */
enum class ThemeMode {
    /** Heller Design-Modus */
    LIGHT,
    /** Dunkler Design-Modus */
    DARK
}

/**
 * Erstellt eine DataStore-Instanz als Extension Property auf dem Context.
 * Dient zur persistenzbasierten Speicherung der Theme-Einstellungen.
 */
private val Context.themeDataStore by preferencesDataStore(name = "theme_preferences")

/**
 * Verwaltet die Benutzereinstellungen für das Erscheinungsbild der App (Hell/Dunkel)
 * mithilfe von Jetpack DataStore.
 */
class ThemePreference(private val context: Context) {

    /**
     * Schlüsselwert für die Speicherung des ausgewählten Design-Modus als String.
     */
    private val themeKey = stringPreferencesKey("theme_mode")

    /**
     * Stellt einen asynchronen Datenstrom (Flow) für den aktuell ausgewählten Design-Modus bereit.
     * Der gespeicherte String wird ausgelesen und in das entsprechende [ThemeMode]-Enum umgewandelt.
     * Falls kein Wert gespeichert ist oder der Wert unbekannt ist, wird standardmäßig [ThemeMode.LIGHT] zurückgegeben.
     */
    val themeMode: Flow<ThemeMode> = context.themeDataStore.data.map { prefs ->
        when (prefs[themeKey]) {
            ThemeMode.DARK.name -> ThemeMode.DARK
            ThemeMode.LIGHT.name -> ThemeMode.LIGHT
            else -> ThemeMode.LIGHT
        }
    }

    /**
     * Aktualisiert den ausgewählten Design-Modus im DataStore.
     * Der Name des übergebenen Enums wird dabei als String gespeichert.
     *
     * @param mode Der gewünschte Design-Modus (LIGHT oder DARK).
     */
    suspend fun setThemeMode(mode: ThemeMode) {
        context.themeDataStore.edit { prefs ->
            prefs[themeKey] = mode.name
        }
    }
}