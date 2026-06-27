package com.example.habithub.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.pomodoroDataStore by preferencesDataStore(name = "pomodoro_preferences")

class PomodoroPreference(private val context: Context) {

    private val workKey = intPreferencesKey("work_minutes")
    private val breakKey = intPreferencesKey("break_minutes")

    val workMinutes: Flow<Int> = context.pomodoroDataStore.data.map { prefs ->
        prefs[workKey] ?: DEFAULT_WORK_MINUTES
    }

    val breakMinutes: Flow<Int> = context.pomodoroDataStore.data.map { prefs ->
        prefs[breakKey] ?: DEFAULT_BREAK_MINUTES
    }

    suspend fun setWorkMinutes(minutes: Int) {
        context.pomodoroDataStore.edit { prefs ->
            prefs[workKey] = minutes.coerceIn(MIN_MINUTES, MAX_MINUTES)
        }
    }

    suspend fun setBreakMinutes(minutes: Int) {
        context.pomodoroDataStore.edit { prefs ->
            prefs[breakKey] = minutes.coerceIn(MIN_MINUTES, MAX_MINUTES)
        }
    }

    companion object {
        const val DEFAULT_WORK_MINUTES = 25
        const val DEFAULT_BREAK_MINUTES = 5
        const val MIN_MINUTES = 1
        const val MAX_MINUTES = 60
    }
}
