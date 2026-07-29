package com.example.habithub.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.habithub.data.preferences.ThemeMode
import com.example.habithub.data.preferences.ThemePreference
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel zur Verwaltung des globalen visuellen Erscheinungsbilds (Theme) der Anwendung.
 *
 * Diese Klasse fungiert als zustandsbehaftete Schnittstelle zwischen der UI-Schicht und
 * den persistierten Theme-Einstellungen im DataStore ([ThemePreference]). Sie stellt den
 * aktuellen Theme-Status als reaktiven Flow bereit und bietet Methoden zur Modifikation.
 *
 * @param themePreference Die Repository-Abstraktion für den Zugriff auf die Theme-Einstellungen im DataStore.
 */
class ThemeViewModel(private val themePreference: ThemePreference) : ViewModel() {

    /**
     * Reaktiver Datenstrom, der den aktuell aktiven Theme-Modus repräsentiert.
     * Nutzt [SharingStarted.WhileSubscribed] mit einem Timeout von 5 Sekunden, um die
     * Beobachtung des DataStores an den Lebenszyklus der konsumierenden UI-Komponenten zu binden
     * und bei Konfigurationsänderungen Ressourcen zu sparen.
     * Der initiale Standardwert ist [ThemeMode.LIGHT].
     */
    val themeMode: StateFlow<ThemeMode> = themePreference.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ThemeMode.LIGHT)

    /**
     * Aktualisiert den Theme-Modus asynchron und speichert diesen persistent im lokalen DataStore.
     *
     * @param mode Der neu anzuwendende [ThemeMode] (z. B. LIGHT oder DARK).
     */
    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { themePreference.setThemeMode(mode) }
    }

    /**
     * Eine Hilfsfunktion zum einfachen Umschalten (Toggeln) zwischen dem hellen und dunklen Modus.
     * Wertet den aktuell gecachten Zustand aus [themeMode] aus und invertiert diesen.
     */
    fun toggleTheme() {
        val next = if (themeMode.value == ThemeMode.DARK) ThemeMode.LIGHT else ThemeMode.DARK
        setThemeMode(next)
    }
}

/**
 * Factory-Klasse zur Instanziierung des [ThemeViewModel]s.
 * Übernimmt die Abhängigkeitsinjektion (Dependency Injection) der [ThemePreference]
 * in den Konstruktor des ViewModels.
 */
class ThemeViewModelFactory(private val themePreference: ThemePreference) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ThemeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ThemeViewModel(themePreference) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}