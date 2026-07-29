package com.example.habithub.ui.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.habithub.R
import com.example.habithub.data.preferences.PomodoroPreference
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Definiert die möglichen Phasen des Pomodoro-Timers.
 *
 * @property labelRes Die Android-Ressourcen-ID (String) für den lokalisierten Anzeigenamen der Phase.
 */
enum class PomodoroPhase(@StringRes val labelRes: Int) {
    WORK(R.string.phase_work),
    BREAK(R.string.phase_break)
}

/**
 * ViewModel zur Verwaltung des Zustands und der Logik des Pomodoro-Timers.
 *
 * Diese Klasse steuert den asynchronen Countdown über Coroutines, verwaltet den automatischen
 * Wechsel zwischen Arbeits- und Pausenphasen und synchronisiert die vom Nutzer eingestellten
 * Zeiten mit dem lokalen DataStore ([PomodoroPreference]).
 *
 * @param preference Die Repository-Abstraktion für den Zugriff auf die Timer-Einstellungen im DataStore.
 */
class PomodoroViewModel(private val preference: PomodoroPreference) : ViewModel() {

    // Konfigurierte Arbeitszeit in Minuten
    private val _workMinutes = MutableStateFlow(PomodoroPreference.DEFAULT_WORK_MINUTES)
    val workMinutes: StateFlow<Int> = _workMinutes.asStateFlow()

    // Konfigurierte Pausenzeit in Minuten
    private val _breakMinutes = MutableStateFlow(PomodoroPreference.DEFAULT_BREAK_MINUTES)
    val breakMinutes: StateFlow<Int> = _breakMinutes.asStateFlow()

    // Die aktuell aktive Timer-Phase (Arbeit oder Pause)
    private val _phase = MutableStateFlow(PomodoroPhase.WORK)
    val phase: StateFlow<PomodoroPhase> = _phase.asStateFlow()

    // Die verbleibende Zeit der aktuellen Phase in Sekunden
    private val _remainingSeconds = MutableStateFlow(PomodoroPreference.DEFAULT_WORK_MINUTES * 60)
    val remainingSeconds: StateFlow<Int> = _remainingSeconds.asStateFlow()

    // Gibt an, ob der Timer momentan herunterzählt
    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    /**
     * Zählt, wie viele Fokus-Runden (Arbeitsphasen) in der aktuellen Sitzung
     * bereits erfolgreich abgeschlossen wurden.
     */
    private val _completedRounds = MutableStateFlow(0)
    val completedRounds: StateFlow<Int> = _completedRounds.asStateFlow()

    // Referenz auf den laufenden Coroutine-Job des Timers, um diesen abbrechen zu können
    private var timerJob: Job? = null

    init {
        // Lädt beim Start asynchron die gespeicherten Zeiteinstellungen aus dem DataStore
        // und initialisiert den Timer-Zustand, sofern dieser nicht bereits läuft.
        viewModelScope.launch {
            _workMinutes.value = preference.workMinutes.first()
            _breakMinutes.value = preference.breakMinutes.first()
            if (!_isRunning.value) {
                _remainingSeconds.value = currentPhaseMinutes() * 60
            }
        }
    }

    /**
     * Startet oder setzt den Countdown-Timer fort.
     * Erzeugt eine Coroutine, die im Sekundentakt den verbleibenden Wert dekrementiert.
     * Sobald die Zeit abläuft, wird automatisch [switchPhase] aufgerufen.
     */
    fun start() {
        if (_isRunning.value) return
        _isRunning.value = true
        timerJob = viewModelScope.launch {
            while (_remainingSeconds.value > 0) {
                delay(1000)
                _remainingSeconds.value -= 1
            }
            // Phase ist abgelaufen -> automatisch umschalten (Lernen <-> Pause)
            switchPhase()
        }
    }

    /**
     * Pausiert den laufenden Timer, indem der zugehörige Coroutine-Job abgebrochen wird.
     * Der aktuelle Stand der verbleibenden Sekunden bleibt erhalten.
     */
    fun pause() {
        _isRunning.value = false
        timerJob?.cancel()
    }

    /**
     * Pausiert den Timer und setzt die verbleibende Zeit auf den initialen Wert
     * der aktuell ausgewählten Phase zurück.
     */
    fun reset() {
        pause()
        _remainingSeconds.value = currentPhaseMinutes() * 60
    }

    /**
     * Wechselt die aktuelle Timer-Phase (von Arbeit zu Pause und umgekehrt).
     * Inkrementiert den Zähler für abgeschlossene Runden, falls eine Arbeitsphase beendet wurde.
     */
    private fun switchPhase() {
        _isRunning.value = false
        // Eine abgeschlossene Lernrunde zählen, bevor in die Pause gewechselt wird
        if (_phase.value == PomodoroPhase.WORK) {
            _completedRounds.value += 1
        }
        _phase.value = if (_phase.value == PomodoroPhase.WORK) PomodoroPhase.BREAK else PomodoroPhase.WORK
        _remainingSeconds.value = currentPhaseMinutes() * 60
    }

    /**
     * @return Die Gesamtminuten der aktuell eingestellten Phase basierend auf den Nutzerpräferenzen.
     */
    private fun currentPhaseMinutes(): Int =
        if (_phase.value == PomodoroPhase.WORK) _workMinutes.value else _breakMinutes.value

    /** Erhöht die konfigurierte Arbeitszeit um 1 Minute. */
    fun increaseWork() = updateWork(_workMinutes.value + 1)

    /** Verringert die konfigurierte Arbeitszeit um 1 Minute. */
    fun decreaseWork() = updateWork(_workMinutes.value - 1)

    /** Erhöht die konfigurierte Pausenzeit um 1 Minute. */
    fun increaseBreak() = updateBreak(_breakMinutes.value + 1)

    /** Verringert die konfigurierte Pausenzeit um 1 Minute. */
    fun decreaseBreak() = updateBreak(_breakMinutes.value - 1)

    /**
     * Aktualisiert die Arbeitszeit im State sowie persistent im DataStore.
     * Stellt sicher, dass die Werte innerhalb der zulässigen Min/Max-Grenzen bleiben.
     * Aktualisiert zudem die verbleibende Timer-Zeit, falls der Timer gerade im Arbeitsmodus pausiert ist.
     */
    private fun updateWork(minutes: Int) {
        val clamped = minutes.coerceIn(PomodoroPreference.MIN_MINUTES, PomodoroPreference.MAX_MINUTES)
        _workMinutes.value = clamped
        viewModelScope.launch { preference.setWorkMinutes(clamped) }
        if (!_isRunning.value && _phase.value == PomodoroPhase.WORK) {
            _remainingSeconds.value = clamped * 60
        }
    }

    /**
     * Aktualisiert die Pausenzeit im State sowie persistent im DataStore.
     * Stellt sicher, dass die Werte innerhalb der zulässigen Min/Max-Grenzen bleiben.
     * Aktualisiert zudem die verbleibende Timer-Zeit, falls der Timer gerade im Pausenmodus pausiert ist.
     */
    private fun updateBreak(minutes: Int) {
        val clamped = minutes.coerceIn(PomodoroPreference.MIN_MINUTES, PomodoroPreference.MAX_MINUTES)
        _breakMinutes.value = clamped
        viewModelScope.launch { preference.setBreakMinutes(clamped) }
        if (!_isRunning.value && _phase.value == PomodoroPhase.BREAK) {
            _remainingSeconds.value = clamped * 60
        }
    }
}

/**
 * Factory-Klasse zur Instanziierung des [PomodoroViewModel]s.
 * Injiziert die [PomodoroPreference]-Abhängigkeit in den Konstruktor des ViewModels.
 */
class PomodoroViewModelFactory(private val preference: PomodoroPreference) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PomodoroViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PomodoroViewModel(preference) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}