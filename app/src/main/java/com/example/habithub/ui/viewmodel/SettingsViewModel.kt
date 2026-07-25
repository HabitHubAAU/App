package com.example.habithub.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.habithub.data.preferences.NotificationPreference
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val notificationPreference: NotificationPreference) : ViewModel() {

    val notificationsEnabled: StateFlow<Boolean> = notificationPreference.notificationsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch { notificationPreference.setNotificationsEnabled(enabled) }
    }
}

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
