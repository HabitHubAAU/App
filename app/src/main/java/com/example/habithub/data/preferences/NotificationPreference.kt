package com.example.habithub.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.notificationDataStore by preferencesDataStore(name = "notification_preferences")

class NotificationPreference(private val context: Context) {

    private val enabledKey = booleanPreferencesKey("notifications_enabled")

    val notificationsEnabled: Flow<Boolean> = context.notificationDataStore.data.map { prefs ->
        prefs[enabledKey] ?: true
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.notificationDataStore.edit { prefs ->
            prefs[enabledKey] = enabled
        }
    }
}
