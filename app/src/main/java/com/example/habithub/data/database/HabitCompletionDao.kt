package com.example.habithub.data.database

import androidx.room.*
import com.example.habithub.data.model.HabitCompletion
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) für die Tabelle "habit_completions".
 * Definiert die Methoden für Lese- und Schreibzugriffe auf die Habit-Abschlüsse in der Room-Datenbank.
 */
@Dao
interface HabitCompletionDao {

    /**
     * Fügt einen neuen Habit-Abschluss in die Datenbank ein.
     * Wird als "suspend"-Funktion deklariert, um asynchron in einer Coroutine ausgeführt zu werden.
     */
    @Insert
    suspend fun insertCompletion(completion: HabitCompletion)

    /**
     * Löscht den Abschluss eines bestimmten Habits für einen definierten Zeitraum (in der Regel für einen bestimmten Tag).
     * Filtert den zu löschenden Eintrag anhand der Habit-ID und dem Zeitfenster zwischen "startOfDay" und "endOfDay".
     */
    @Query("DELETE FROM habit_completions WHERE habitId = :habitId AND completedAt >= :startOfDay AND completedAt < :endOfDay")
    suspend fun deleteCompletionForDay(habitId: Int, startOfDay: Long, endOfDay: Long)

    /**
     * Ruft alle Habit-Abschlüsse ab, die innerhalb eines bestimmten Zeitfensters liegen (z. B. der heutige Tag).
     * Gibt einen asynchronen Datenstrom (Flow) zurück, der die UI automatisch aktualisiert, wenn sich die Daten ändern.
     */
    @Query("SELECT * FROM habit_completions WHERE completedAt >= :startOfDay AND completedAt < :endOfDay")
    fun getCompletionsForDay(startOfDay: Long, endOfDay: Long): Flow<List<HabitCompletion>>

    /**
     * Ruft alle Habit-Abschlüsse ab, die ab einem bestimmten Zeitstempel ("since") erfolgt sind.
     * Sortiert die Ergebnisse chronologisch absteigend (neueste zuerst) und gibt sie als Flow zurück.
     */
    @Query("SELECT * FROM habit_completions WHERE completedAt >= :since ORDER BY completedAt DESC")
    fun getCompletionsSince(since: Long): Flow<List<HabitCompletion>>
}