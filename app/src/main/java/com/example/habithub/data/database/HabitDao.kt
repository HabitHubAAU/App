package com.example.habithub.data.database

import androidx.room.*
import com.example.habithub.data.model.Habit
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) für die Tabelle "habits".
 * Definiert die grundlegenden Datenbankoperationen (CRUD) für die Gewohnheiten.
 */
@Dao
interface HabitDao {

    /**
     * Ruft alle gespeicherten Habits aus der Datenbank ab.
     * Die Ergebnisse werden aufsteigend nach dem Erstellungsdatum sortiert.
     * Die Rückgabe als Flow ermöglicht es der UI, auf zukünftige Datenänderungen in Echtzeit zu reagieren.
     */
    @Query("SELECT * FROM habits ORDER BY createdAt ASC")
    fun getAllHabits(): Flow<List<Habit>>

    /**
     * Fügt einen neuen Habit in die Datenbank ein.
     * Bei einem Konflikt (z. B. wenn die ID bereits existiert) wird der alte Eintrag durch den neuen ersetzt (REPLACE).
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit)

    /**
     * Aktualisiert einen bestehenden Habit in der Datenbank.
     * Der Abgleich erfolgt dabei anhand des Primärschlüssels (ID) des übergebenen Objekts.
     */
    @Update
    suspend fun updateHabit(habit: Habit)

    /**
     * Löscht den spezifizierten Habit aus der Datenbank.
     * Der Abgleich erfolgt ebenfalls über den Primärschlüssel.
     */
    @Delete
    suspend fun deleteHabit(habit: Habit)

    /**
     * Sucht einen spezifischen Habit anhand seiner eindeutigen ID.
     * Es wird maximal ein Ergebnis zurückgegeben (LIMIT 1).
     * Ist die ID nicht vorhanden, wird null zurückgegeben.
     */
    @Query("SELECT * FROM habits WHERE id = :habitId LIMIT 1")
    suspend fun getHabitById(habitId: Int): Habit?
}