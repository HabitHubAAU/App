package com.example.habithub.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.habithub.data.preferences.NotificationPreference
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel zur Verwaltung der globalen App-Einstellungen.
 *
 * Diese Klasse fungiert als zustandsbehaftetes Bindeglied zwischen der Konfigurations-UI
 * (wie dem SettingsScreen) und den lokalen Nutzerpräferenzen (DataStore). Aktuell fokussiert
 * sich das ViewModel auf die Steuerung der globalen Benachrichtigungseinstellungen.
 *
 * @param notificationPreference Die Repository-Abstraktion für den Zugriff auf die
 * Benachrichtigungseinstellungen im DataStore.
 */
class SettingsViewModel(private val notificationPreference: NotificationPreference) : ViewModel() {

    /**
     * Reaktiver Datenstrom, der den aktuellen Status der globalen Benachrichtigungsfreigabe repräsentiert.
     * Nutzt [SharingStarted.WhileSubscribed] mit einem Timeout von 5 Sekunden, um den
     * zugrundeliegenden Flow aus dem DataStore ressourcenschonend an den UI-Lebenszyklus zu binden.
     */
    val notificationsEnabled: StateFlow<Boolean> = notificationPreference.notificationsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    /**
     * Aktualisiert den Status der Benachrichtigungsfreigabe asynchron und speichert
     * diesen persistent im lokalen DataStore.
     *
     * @param enabled Der neue Wahrheitswert für die Benachrichtigungsfreigabe (true = aktiviert, false = deaktiviert).
     */
    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch { notificationPreference.setNotificationsEnabled(enabled) }
    }
}

/**
 * Factory-Klasse zur Instanziierung des [SettingsViewModel]s.
 * Injiziert die [NotificationPreference]-Abhängigkeit (DataStore-Zugriff) in den Konstruktor des ViewModels.
 */
class SettingsViewModelFactory(
    private val notificationPreference: NotificationPreference
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(notificationPreference) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}