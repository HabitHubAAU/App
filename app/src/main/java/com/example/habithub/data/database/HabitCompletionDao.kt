package com.example.habithub.data.database

import androidx.room.*
import com.example.habithub.data.model.HabitCompletion
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitCompletionDao {
    @Insert
    suspend fun insertCompletion(completion: HabitCompletion)

    @Query("DELETE FROM habit_completions WHERE habitId = :habitId AND completedAt >= :startOfDay AND completedAt < :endOfDay")
    suspend fun deleteCompletionForDay(habitId: Int, startOfDay: Long, endOfDay: Long)

    @Query("SELECT * FROM habit_completions WHERE completedAt >= :startOfDay AND completedAt < :endOfDay")
    fun getCompletionsForDay(startOfDay: Long, endOfDay: Long): Flow<List<HabitCompletion>>

    @Query("SELECT * FROM habit_completions WHERE completedAt >= :since ORDER BY completedAt DESC")
    fun getCompletionsSince(since: Long): Flow<List<HabitCompletion>>
}
