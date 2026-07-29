package com.example.habithub.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Erstellt eine DataStore-Instanz als Extension Property auf dem Context.
 * Dient zur persistenzbasierten Speicherung der Benachrichtigungseinstellungen.
 */
private val Context.notificationDataStore by preferencesDataStore(name = "notification_preferences")

/**
 * Verwaltet die Benutzereinstellungen für App-Benachrichtigungen mithilfe von Jetpack DataStore.
 */
class NotificationPreference(private val context: Context) {

    /**
     * Schlüsselwert zum Speichern und Abrufen des Aktivierungsstatus der Benachrichtigungen.
     */
    private val enabledKey = booleanPreferencesKey("notifications_enabled")

    /**
     * Stellt einen asynchronen Datenstrom (Flow) bereit, der den aktuellen Status der Benachrichtigungen ausgibt.
     * Wenn noch kein Wert im DataStore existiert, wird standardmäßig 'true' zurückgegeben.
     */
    val notificationsEnabled: Flow<Boolean> = context.notificationDataStore.data.map { prefs ->
        prefs[enabledKey] ?: true
    }

    /**
     * Aktualisiert den Aktivierungsstatus der Benachrichtigungen im DataStore.
     * Da das Schreiben auf den Datenträger eine I/O-Operation ist, wird dies asynchron als Suspend-Funktion ausgeführt.
     *
     * @param enabled Der neue Status (true für aktiviert, false für deaktiviert).
     */
    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.notificationDataStore.edit { prefs ->
            prefs[enabledKey] = enabled
        }
    }
}