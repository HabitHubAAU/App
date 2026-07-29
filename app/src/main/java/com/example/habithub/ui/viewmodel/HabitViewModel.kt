package com.example.habithub.ui.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.habithub.R
import com.example.habithub.data.model.Habit
import com.example.habithub.data.model.HabitCompletion
import com.example.habithub.data.repository.HabitRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * Definiert die verfügbaren Sortierkriterien für die Darstellung der Gewohnheitsliste.
 *
 * @property labelRes Die Android-Ressourcen-ID (String) für den lokalisierten Anzeigenamen der Sortierung.
 */
enum class SortOrder(@StringRes val labelRes: Int) {
    DEFAULT(R.string.sort_default),
    NAME(R.string.sort_name),
    STREAK(R.string.sort_streak),
    COMPLETION_RATE(R.string.sort_completion_rate)
}

/**
 * Das zentrale ViewModel zur Verwaltung der Gewohnheitsdaten und Ausführung der Geschäftslogik.
 *
 * Es fungiert als zustandsbehaftetes Bindeglied zwischen der UI-Schicht und dem [HabitRepository].
 * Die Klasse transformiert asynchrone Datenströme der Datenbank in persistente UI-Zustände
 * (StateFlows) und stellt Funktionen zur Manipulation der Daten sowie zur Berechnung komplexer
 * Metriken (wie Streaks und Abschlussraten) bereit.
 *
 * @param repository Das Repository zur Abstraktion der lokalen Datenquelle (Room-Datenbank).
 */
class HabitViewModel(private val repository: HabitRepository) : ViewModel() {

    /**
     * Reaktiver Datenstrom aller gespeicherten Gewohnheiten.
     * Nutzt [SharingStarted.WhileSubscribed] mit einem Timeout von 5 Sekunden, um
     * bei Konfigurationsänderungen (z. B. Bildschirmdrehung) unnötige Datenbankabfragen zu vermeiden.
     */
    val habits: StateFlow<List<Habit>> = repository.getAllHabits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /**
     * Reaktiver Datenstrom aller heutigen Gewohnheitsabschlüsse.
     */
    val todayCompletions: StateFlow<List<HabitCompletion>> = repository.getTodayCompletions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /**
     * Reaktiver Datenstrom der Gewohnheitsabschlüsse der letzten 90 Tage.
     * Dient als Grundlage für die meisten statistischen Berechnungen in der UI.
     */
    val recentCompletions: StateFlow<List<HabitCompletion>> = repository.getCompletionsSince(90)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Backing Property für die Sortierreihenfolge
    private val _sortOrder = MutableStateFlow(SortOrder.DEFAULT)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    /**
     * Kombinierter Datenstrom, der die Gewohnheiten basierend auf der aktuellen [SortOrder] dynamisch neu anordnet.
     * Die Flow-Kombination reagiert automatisch auf Änderungen der Gewohnheiten, der Sortierreihenfolge
     * oder der Abschlusshistorie (welche für Streak-basierte Sortierungen benötigt wird).
     */
    val sortedHabits: StateFlow<List<Habit>> = combine(habits, _sortOrder, recentCompletions) { hs, order, completions ->
        when (order) {
            SortOrder.DEFAULT -> hs
            SortOrder.NAME -> hs.sortedBy { it.name.lowercase() }
            SortOrder.STREAK -> hs.sortedByDescending { calculateStreak(it.id, completions) }
            SortOrder.COMPLETION_RATE -> hs.sortedByDescending { calculateCompletionRate(it, completions) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /**
     * Aktualisiert das globale Sortierkriterium.
     * Löst eine Neuberechnung des [sortedHabits]-Datenstroms aus.
     */
    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
    }

    /**
     * Erstellt eine neue Gewohnheit und speichert diese asynchron in der Datenbank.
     */
    fun addHabit(name: String, description: String, emoji: String, colorValue: Long, targetDays: Int, category: String = "hobby") {
        viewModelScope.launch {
            repository.insertHabit(
                Habit(
                    name = name.trim(),
                    description = description.trim(),
                    emoji = emoji,
                    colorValue = colorValue,
                    targetDays = targetDays,
                    category = category
                )
            )
        }
    }

    /**
     * Aktualisiert eine bestehende Gewohnheit asynchron in der Datenbank.
     */
    fun updateHabit(habit: Habit) {
        viewModelScope.launch { repository.updateHabit(habit) }
    }

    /**
     * Löscht eine Gewohnheit sowie asynchron alle damit verbundenen Historien-Einträge (Cascade).
     */
    fun deleteHabit(habit: Habit) {
        viewModelScope.launch { repository.deleteHabit(habit) }
    }

    /**
     * Wechselt den Abschlussstatus einer Gewohnheit für den aktuellen Tag.
     * Prüft anhand des zwischengespeicherten Zustands [todayCompletions], ob die Gewohnheit
     * bereits als erledigt markiert ist, und führt die entsprechende gegenteilige Operation aus.
     */
    fun toggleCompletion(habit: Habit) {
        val isCompleted = todayCompletions.value.any { it.habitId == habit.id }
        viewModelScope.launch { repository.toggleCompletion(habit.id, isCompleted) }
    }

    /**
     * Ruft eine einzelne Gewohnheit anhand ihrer ID synchron aus der Datenbank ab.
     */
    suspend fun getHabitById(habitId: Int): Habit? = repository.getHabitById(habitId)

    /**
     * Berechnet die aktuell ununterbrochene Serie (Streak) an Tagen für eine bestimmte Gewohnheit.
     * Die Zählung beginnt beim heutigen Tag und iteriert rückwärts, bis eine Lücke in den
     * Tages-Abschlüssen gefunden wird.
     *
     * @param habitId Die ID der zu prüfenden Gewohnheit.
     * @param completions Die Liste der auszuwertenden historischen Abschlüsse.
     * @return Die Anzahl der aufeinanderfolgenden Tage, an denen die Gewohnheit ausgeführt wurde.
     */
    fun calculateStreak(habitId: Int, completions: List<HabitCompletion>): Int {
        val days = completions.filter { it.habitId == habitId }
            .map { startOfDay(it.completedAt) }.toSet()

        val cursor = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        var streak = 0
        while (cursor.timeInMillis in days) {
            streak++
            cursor.add(Calendar.DAY_OF_YEAR, -1)
        }
        return streak
    }

    /**
     * Berechnet die historisch längste ununterbrochene Serie (Best Streak) für eine Gewohnheit.
     * Vergleicht sortierte, normierte Zeitstempel und inkrementiert den Zähler,
     * solange die Differenz exakt 86.400.000 Millisekunden (1 Tag) beträgt.
     *
     * @param habitId Die ID der zu prüfenden Gewohnheit.
     * @param completions Die Liste der auszuwertenden historischen Abschlüsse.
     * @return Die höchste jemals erreichte Anzahl an aufeinanderfolgenden Ausführungstagen.
     */
    fun calculateBestStreak(habitId: Int, completions: List<HabitCompletion>): Int {
        val dayList = completions.filter { it.habitId == habitId }
            .map { startOfDay(it.completedAt) }.toSortedSet().toList()
        if (dayList.isEmpty()) return 0
        var best = 1; var current = 1
        for (i in 1 until dayList.size) {
            // Differenz von exakt einem Tag (in Millisekunden)
            if (dayList[i] - dayList[i - 1] == 86_400_000L) {
                current++
                if (current > best) best = current
            } else {
                current = 1
            }
        }
        return best
    }

    /**
     * Berechnet die Erfolgsquote einer Gewohnheit relativ zu ihrer Existenzdauer.
     *
     * @param habit Das Gewohnheits-Modell (benötigt für das Erstellungsdatum).
     * @param completions Die Liste der auszuwertenden historischen Abschlüsse.
     * @return Ein Float-Wert zwischen 0.0f (0%) und 1.0f (100%).
     */
    fun calculateCompletionRate(habit: Habit, completions: List<HabitCompletion>): Float {
        val daysTotal = ((System.currentTimeMillis() - habit.createdAt) / 86_400_000L + 1)
            .toInt().coerceAtLeast(1)
        val completed = completions.filter { it.habitId == habit.id }
            .map { startOfDay(it.completedAt) }.toSet().size
        return completed.toFloat() / daysTotal.toFloat()
    }

    /**
     * Ermittelt den Abschlussstatus der letzten 7 Tage (inklusive heute).
     * Diese Datenstruktur wird typischerweise für die Darstellung von wöchentlichen Balkendiagrammen genutzt.
     *
     * @param habitId Die ID der auszuwertenden Gewohnheit.
     * @param completions Die Liste der historischen Abschlüsse.
     * @return Eine Liste von 7 booleschen Werten, geordnet von alt (Index 0) nach heute (Index 6).
     */
    fun getWeeklyData(habitId: Int, completions: List<HabitCompletion>): List<Boolean> {
        val days = completions.filter { it.habitId == habitId }
            .map { startOfDay(it.completedAt) }.toSet()
        return (6 downTo 0).map { daysAgo ->
            val cal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -daysAgo)
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }
            cal.timeInMillis in days
        }
    }

    /**
     * Ermittelt den Abschlussstatus der letzten 30 Tage.
     *
     * @param habitId Die ID der auszuwertenden Gewohnheit.
     * @param completions Die Liste der historischen Abschlüsse.
     * @return Eine chronologische Liste von 30 booleschen Werten, endend am heutigen Tag.
     */
    fun get30DayData(habitId: Int, completions: List<HabitCompletion>): List<Boolean> {
        val days = completions.filter { it.habitId == habitId }
            .map { startOfDay(it.completedAt) }.toSet()
        return (29 downTo 0).map { daysAgo ->
            val cal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -daysAgo)
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }
            cal.timeInMillis in days
        }
    }

    /**
     * Hilfsfunktion zur Normalisierung von Zeitstempeln.
     * Setzt die Uhrzeit-Komponenten (Stunden, Minuten, Sekunden, Millisekunden) auf 0 (Mitternacht),
     * um absolute Tagesvergleiche unabhängig von der genauen Erfassungszeit zu ermöglichen.
     *
     * @param timestamp Der ursprüngliche Unix-Zeitstempel in Millisekunden.
     * @return Der bereinigte Unix-Zeitstempel (Beginn des zugehörigen Tages).
     */
    private fun startOfDay(timestamp: Long): Long = Calendar.getInstance().apply {
        timeInMillis = timestamp
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

/**
 * Factory-Klasse zur Instanziierung des [HabitViewModel]s.
 * Erforderlich, da das ViewModel Parameter im Konstruktor (das Repository) benötigt,
 * welche standardmäßig nicht vom Android-System aufgelöst werden können.
 */
class HabitViewModelFactory(private val repository: HabitRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HabitViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HabitViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}