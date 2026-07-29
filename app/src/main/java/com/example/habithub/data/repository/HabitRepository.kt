package com.example.habithub.data.repository

import com.example.habithub.data.database.HabitCompletionDao
import com.example.habithub.data.database.HabitDao
import com.example.habithub.data.model.Habit
import com.example.habithub.data.model.HabitCompletion
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

/**
 * Vermittelt als Repository zwischen den Datenquellen (DAOs) und der restlichen Anwendungsarchitektur.
 * Bündelt die Zugriffe und Geschäftslogik für Gewohnheiten (Habits) und deren Abschlussstatus.
 */
class HabitRepository(
    private val habitDao: HabitDao,
    private val completionDao: HabitCompletionDao
) {
    /**
     * Ruft einen asynchronen Datenstrom (Flow) aller in der Datenbank gespeicherten Gewohnheiten ab.
     */
    fun getAllHabits(): Flow<List<Habit>> = habitDao.getAllHabits()

    /**
     * Fügt eine neue Gewohnheit asynchron in die Datenbank ein.
     */
    suspend fun insertHabit(habit: Habit) = habitDao.insertHabit(habit)

    /**
     * Aktualisiert die Daten einer bestehenden Gewohnheit in der Datenbank.
     */
    suspend fun updateHabit(habit: Habit) = habitDao.updateHabit(habit)

    /**
     * Löscht eine spezifische Gewohnheit aus der Datenbank.
     */
    suspend fun deleteHabit(habit: Habit) = habitDao.deleteHabit(habit)

    /**
     * Ruft eine spezifische Gewohnheit anhand ihrer ID ab.
     * Gibt null zurück, falls keine Gewohnheit mit dieser ID existiert.
     */
    suspend fun getHabitById(habitId: Int): Habit? = habitDao.getHabitById(habitId)

    /**
     * Ruft alle Habit-Abschlüsse des aktuellen Tages als kontinuierlichen Flow ab.
     * Nutzt [todayBounds] zur Ermittlung des heutigen Zeitfensters.
     */
    fun getTodayCompletions(): Flow<List<HabitCompletion>> {
        val (start, end) = todayBounds()
        return completionDao.getCompletionsForDay(start, end)
    }

    /**
     * Ermittelt alle Habit-Abschlüsse der letzten [days] Tage.
     * Berechnet dazu den Startzeitpunkt (Mitternacht) des entsprechenden Tages in der Vergangenheit.
     *
     * @param days Die Anzahl der Tage, die in die Vergangenheit geschaut werden soll.
     */
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

    /**
     * Wechselt den Abschlussstatus einer Gewohnheit für den heutigen Tag.
     * Löscht den Abschlusseintrag aus der Datenbank, falls die Gewohnheit heute bereits abgeschlossen wurde.
     * Fügt einen neuen Abschlusseintrag hinzu, falls sie noch nicht abgeschlossen war.
     *
     * @param habitId Die ID der betreffenden Gewohnheit.
     * @param isCurrentlyCompleted Der momentane Abschlussstatus (true = bereits abgeschlossen).
     */
    suspend fun toggleCompletion(habitId: Int, isCurrentlyCompleted: Boolean) {
        val (start, end) = todayBounds()
        if (isCurrentlyCompleted) {
            completionDao.deleteCompletionForDay(habitId, start, end)
        } else {
            completionDao.insertCompletion(HabitCompletion(habitId = habitId))
        }
    }

    /**
     * Hilfsmethode zur Berechnung der Zeitgrenzen des aktuellen Tages.
     *
     * @return Ein [Pair], das den Startzeitpunkt (Mitternacht) und den
     * Endzeitpunkt (Mitternacht plus 24 Stunden) des heutigen Tages in Millisekunden enthält.
     */
    private fun todayBounds(): Pair<Long, Long> {
        val start = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        // 86_400_000 Millisekunden entsprechen genau 24 Stunden
        return Pair(start, start + 86_400_000L)
    }
}