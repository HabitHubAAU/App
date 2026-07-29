package com.example.habithub.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Erstellt eine DataStore-Instanz zur Speicherung der Pomodoro-spezifischen Einstellungen.
 */
private val Context.pomodoroDataStore by preferencesDataStore(name = "pomodoro_preferences")

/**
 * Verwaltet die Benutzereinstellungen für den Pomodoro-Timer (Arbeits- und Pausenzeiten)
 * mithilfe von Jetpack DataStore.
 */
class PomodoroPreference(private val context: Context) {

    /**
     * Schlüsselwert für die Speicherung der Dauer einer Arbeitsphase.
     */
    private val workKey = intPreferencesKey("work_minutes")

    /**
     * Schlüsselwert für die Speicherung der Dauer einer Pausenphase.
     */
    private val breakKey = intPreferencesKey("break_minutes")

    /**
     * Stellt einen asynchronen Datenstrom (Flow) für die eingestellte Arbeitszeit bereit.
     * Falls kein Wert gespeichert ist, wird [DEFAULT_WORK_MINUTES] zurückgegeben.
     */
    val workMinutes: Flow<Int> = context.pomodoroDataStore.data.map { prefs ->
        prefs[workKey] ?: DEFAULT_WORK_MINUTES
    }

    /**
     * Stellt einen asynchronen Datenstrom (Flow) für die eingestellte Pausenzeit bereit.
     * Falls kein Wert gespeichert ist, wird [DEFAULT_BREAK_MINUTES] zurückgegeben.
     */
    val breakMinutes: Flow<Int> = context.pomodoroDataStore.data.map { prefs ->
        prefs[breakKey] ?: DEFAULT_BREAK_MINUTES
    }

    /**
     * Aktualisiert die Dauer der Arbeitsphase im DataStore.
     * Der übergebene Wert wird dabei durch `coerceIn` automatisch auf den zulässigen
     * Bereich zwischen [MIN_MINUTES] und [MAX_MINUTES] begrenzt.
     *
     * @param minutes Die gewünschte Arbeitszeit in Minuten.
     */
    suspend fun setWorkMinutes(minutes: Int) {
        context.pomodoroDataStore.edit { prefs ->
            prefs[workKey] = minutes.coerceIn(MIN_MINUTES, MAX_MINUTES)
        }
    }

    /**
     * Aktualisiert die Dauer der Pausenphase im DataStore.
     * Der übergebene Wert wird dabei durch `coerceIn` automatisch auf den zulässigen
     * Bereich zwischen [MIN_MINUTES] und [MAX_MINUTES] begrenzt.
     *
     * @param minutes Die gewünschte Pausenzeit in Minuten.
     */
    suspend fun setBreakMinutes(minutes: Int) {
        context.pomodoroDataStore.edit { prefs ->
            prefs[breakKey] = minutes.coerceIn(MIN_MINUTES, MAX_MINUTES)
        }
    }

    /**
     * Definiert die Standard-, Minimal- und Maximalwerte für die Timer-Konfiguration.
     */
    companion object {
        /** Standardmäßige Dauer einer Arbeitsphase (25 Minuten). */
        const val DEFAULT_WORK_MINUTES = 25
        /** Standardmäßige Dauer einer Pausenphase (5 Minuten). */
        const val DEFAULT_BREAK_MINUTES = 5
        /** Minimal zulässige Dauer für Arbeits- und Pausenphasen. */
        const val MIN_MINUTES = 1
        /** Maximal zulässige Dauer für Arbeits- und Pausenphasen. */
        const val MAX_MINUTES = 60
    }
}