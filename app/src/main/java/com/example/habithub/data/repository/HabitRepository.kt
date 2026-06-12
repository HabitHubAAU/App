package com.example.habithub.data.repository

import com.example.habithub.data.database.HabitCompletionDao
import com.example.habithub.data.database.HabitDao
import com.example.habithub.data.model.Habit
import com.example.habithub.data.model.HabitCompletion
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class HabitRepository(
    private val habitDao: HabitDao,
    private val completionDao: HabitCompletionDao
) {
    fun getAllHabits(): Flow<List<Habit>> = habitDao.getAllHabits()

    suspend fun insertHabit(habit: Habit) = habitDao.insertHabit(habit)
    suspend fun updateHabit(habit: Habit) = habitDao.updateHabit(habit)
    suspend fun deleteHabit(habit: Habit) = habitDao.deleteHabit(habit)
    suspend fun getHabitById(habitId: Int): Habit? = habitDao.getHabitById(habitId)

    fun getTodayCompletions(): Flow<List<HabitCompletion>> {
        val (start, end) = todayBounds()
        return completionDao.getCompletionsForDay(start, end)
    }

    fun getCompletionsSince(days: Int): Flow<List<HabitCompletion>> {
        val since = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -days)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        return completionDao.getCompletionsSince(since)
    }

    suspend fun toggleCompletion(habitId: Int, isCurrentlyCompleted: Boolean) {
        val (start, end) = todayBounds()
        if (isCurrentlyCompleted) {
            completionDao.deleteCompletionForDay(habitId, start, end)
        } else {
            completionDao.insertCompletion(HabitCompletion(habitId = habitId))
        }
    }

    private fun todayBounds(): Pair<Long, Long> {
        val start = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        return Pair(start, start + 86_400_000L)
    }
}
