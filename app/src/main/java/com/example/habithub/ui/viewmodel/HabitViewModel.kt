package com.habithub.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.habithub.data.model.Habit
import com.example.habithub.data.model.HabitCompletion
import com.example.habithub.data.repository.HabitRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

enum class SortOrder(val label: String) {
    DEFAULT("Default"),
    NAME("By Name"),
    STREAK("By Streak"),
    COMPLETION_RATE("By Completion Rate")
}

class HabitViewModel(private val repository: HabitRepository) : ViewModel() {

    val habits: StateFlow<List<Habit>> = repository.getAllHabits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val todayCompletions: StateFlow<List<HabitCompletion>> = repository.getTodayCompletions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val recentCompletions: StateFlow<List<HabitCompletion>> = repository.getCompletionsSince(90)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _sortOrder = MutableStateFlow(SortOrder.DEFAULT)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    val sortedHabits: StateFlow<List<Habit>> = combine(habits, _sortOrder, recentCompletions) { hs, order, completions ->
        when (order) {
            SortOrder.DEFAULT -> hs
            SortOrder.NAME -> hs.sortedBy { it.name.lowercase() }
            SortOrder.STREAK -> hs.sortedByDescending { calculateStreak(it.id, completions) }
            SortOrder.COMPLETION_RATE -> hs.sortedByDescending { calculateCompletionRate(it, completions) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
    }

    fun addHabit(name: String, description: String, emoji: String, colorValue: Long, targetDays: Int) {
        viewModelScope.launch {
            repository.insertHabit(
                Habit(
                    name = name.trim(),
                    description = description.trim(),
                    emoji = emoji,
                    colorValue = colorValue,
                    targetDays = targetDays
                )
            )
        }
    }

    fun updateHabit(habit: Habit) {
        viewModelScope.launch { repository.updateHabit(habit) }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch { repository.deleteHabit(habit) }
    }

    fun toggleCompletion(habit: Habit) {
        val isCompleted = todayCompletions.value.any { it.habitId == habit.id }
        viewModelScope.launch { repository.toggleCompletion(habit.id, isCompleted) }
    }

    suspend fun getHabitById(habitId: Int): Habit? = repository.getHabitById(habitId)

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

    fun calculateBestStreak(habitId: Int, completions: List<HabitCompletion>): Int {
        val dayList = completions.filter { it.habitId == habitId }
            .map { startOfDay(it.completedAt) }.toSortedSet().toList()
        if (dayList.isEmpty()) return 0
        var best = 1; var current = 1
        for (i in 1 until dayList.size) {
            if (dayList[i] - dayList[i - 1] == 86_400_000L) {
                current++
                if (current > best) best = current
            } else {
                current = 1
            }
        }
        return best
    }

    fun calculateCompletionRate(habit: Habit, completions: List<HabitCompletion>): Float {
        val daysTotal = ((System.currentTimeMillis() - habit.createdAt) / 86_400_000L + 1)
            .toInt().coerceAtLeast(1)
        val completed = completions.filter { it.habitId == habit.id }
            .map { startOfDay(it.completedAt) }.toSet().size
        return completed.toFloat() / daysTotal.toFloat()
    }

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

    private fun startOfDay(timestamp: Long): Long = Calendar.getInstance().apply {
        timeInMillis = timestamp
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

class HabitViewModelFactory(private val repository: HabitRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HabitViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HabitViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
