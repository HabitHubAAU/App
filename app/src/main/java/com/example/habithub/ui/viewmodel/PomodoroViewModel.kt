package com.example.habithub.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.habithub.data.preferences.PomodoroPreference
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

enum class PomodoroPhase(val label: String) {
    WORK("Lernen"),
    BREAK("Pause")
}

class PomodoroViewModel(private val preference: PomodoroPreference) : ViewModel() {

    private val _workMinutes = MutableStateFlow(PomodoroPreference.DEFAULT_WORK_MINUTES)
    val workMinutes: StateFlow<Int> = _workMinutes.asStateFlow()

    private val _breakMinutes = MutableStateFlow(PomodoroPreference.DEFAULT_BREAK_MINUTES)
    val breakMinutes: StateFlow<Int> = _breakMinutes.asStateFlow()

    private val _phase = MutableStateFlow(PomodoroPhase.WORK)
    val phase: StateFlow<PomodoroPhase> = _phase.asStateFlow()

    private val _remainingSeconds = MutableStateFlow(PomodoroPreference.DEFAULT_WORK_MINUTES * 60)
    val remainingSeconds: StateFlow<Int> = _remainingSeconds.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    // Zählt, wie viele Lernrunden bereits abgeschlossen wurden
    private val _completedRounds = MutableStateFlow(0)
    val completedRounds: StateFlow<Int> = _completedRounds.asStateFlow()

    private var timerJob: Job? = null

    init {
        // Gespeicherte Zeiten laden und den Timer initial darauf setzen
        viewModelScope.launch {
            _workMinutes.value = preference.workMinutes.first()
            _breakMinutes.value = preference.breakMinutes.first()
            if (!_isRunning.value) {
                _remainingSeconds.value = currentPhaseMinutes() * 60
            }
        }
    }

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

    fun pause() {
        _isRunning.value = false
        timerJob?.cancel()
    }

    fun reset() {
        pause()
        _remainingSeconds.value = currentPhaseMinutes() * 60
    }

    private fun switchPhase() {
        _isRunning.value = false
        // Eine abgeschlossene Lernrunde zählen, bevor in die Pause gewechselt wird
        if (_phase.value == PomodoroPhase.WORK) {
            _completedRounds.value += 1
        }
        _phase.value = if (_phase.value == PomodoroPhase.WORK) PomodoroPhase.BREAK else PomodoroPhase.WORK
        _remainingSeconds.value = currentPhaseMinutes() * 60
    }

    private fun currentPhaseMinutes(): Int =
        if (_phase.value == PomodoroPhase.WORK) _workMinutes.value else _breakMinutes.value

    fun increaseWork() = updateWork(_workMinutes.value + 1)
    fun decreaseWork() = updateWork(_workMinutes.value - 1)
    fun increaseBreak() = updateBreak(_breakMinutes.value + 1)
    fun decreaseBreak() = updateBreak(_breakMinutes.value - 1)

    private fun updateWork(minutes: Int) {
        val clamped = minutes.coerceIn(PomodoroPreference.MIN_MINUTES, PomodoroPreference.MAX_MINUTES)
        _workMinutes.value = clamped
        viewModelScope.launch { preference.setWorkMinutes(clamped) }
        if (!_isRunning.value && _phase.value == PomodoroPhase.WORK) {
            _remainingSeconds.value = clamped * 60
        }
    }

    private fun updateBreak(minutes: Int) {
        val clamped = minutes.coerceIn(PomodoroPreference.MIN_MINUTES, PomodoroPreference.MAX_MINUTES)
        _breakMinutes.value = clamped
        viewModelScope.launch { preference.setBreakMinutes(clamped) }
        if (!_isRunning.value && _phase.value == PomodoroPhase.BREAK) {
            _remainingSeconds.value = clamped * 60
        }
    }
}

class PomodoroViewModelFactory(private val preference: PomodoroPreference) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PomodoroViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PomodoroViewModel(preference) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
