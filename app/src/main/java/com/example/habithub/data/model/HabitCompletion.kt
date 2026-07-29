package com.example.habithub.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Repräsentiert den Abschluss einer Gewohnheit als Entität, die auf die Tabelle "habit_completions" abgebildet wird.
 * Protokolliert, wann eine bestimmte Gewohnheit als erledigt markiert wurde.
 */
@Entity(
    tableName = "habit_completions",
    foreignKeys = [
        /**
         * Definiert eine Fremdschlüsselbeziehung zur "habits"-Tabelle.
         * Durch onDelete = CASCADE wird sichergestellt, dass beim Löschen eines Habits
         * automatisch auch alle dazugehörigen Abschluss-Einträge aus der Datenbank entfernt werden.
         */
        ForeignKey(
            entity = Habit::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.Companion.CASCADE
        )
    ],
    /**
     * Erstellt einen Datenbank-Index für die Fremdschlüssel-Spalte "habitId".
     * Dies optimiert die Ausführungsgeschwindigkeit von Abfragen, die Daten anhand der Habit-ID filtern oder verknüpfen.
     */
    indices = [Index("habitId")]
)
data class HabitCompletion(
    /**
     * Der eindeutige Primärschlüssel für diesen Abschluss-Eintrag.
     * Wird beim Einfügen in die Datenbank automatisch generiert.
     */
    @PrimaryKey(autoGenerate = true) val id: Int = 0,

    /**
     * Die ID der verknüpften Gewohnheit (Referenz auf die "habits"-Tabelle).
     */
    val habitId: Int,

    /**
     * Der Zeitpunkt der Erledigung.
     * Gespeichert als Millisekunden seit der Unix-Epoche (1. Januar 1970).
     */
    val completedAt: Long = System.currentTimeMillis()
)