package com.example.habithub.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Repräsentiert eine Habit-Entität (Gewohnheit), die auf die Tabelle "habits" in der Room-Datenbank abgebildet wird.
 * Definiert das Datenschema und die Standardwerte für die einzelnen Einträge.
 */
@Entity(tableName = "habits")
data class Habit(
    /**
     * Der eindeutige Primärschlüssel für den Eintrag.
     * Wird beim Einfügen in die Datenbank automatisch generiert.
     */
    @PrimaryKey(autoGenerate = true) val id: Int = 0,

    /**
     * Der primäre Anzeigename der Gewohnheit.
     */
    val name: String,

    /**
     * Zusätzliche Details oder Notizen zur Gewohnheit.
     * Standardmäßig ein leerer String.
     */
    val description: String = "",

    /**
     * Ein einzelnes Emoji-Zeichen, das als visuelle Kennzeichnung in der UI dient.
     * Standardmäßig ein Stern-Symbol.
     */
    val emoji: String = "⭐",

    /**
     * Die mit der Gewohnheit verknüpfte Farbe.
     * Wird als Long-Wert gespeichert, der einen ARGB-Farbwert repräsentiert.
     */
    val colorValue: Long = 0xFF6750A4L,

    /**
     * Ein Bitmasken-Integer, der die anvisierten Wochentage für die Gewohnheit darstellt.
     * Der Standardwert (0b1111111) gibt an, dass alle 7 Wochentage als Ziel gesetzt sind.
     */
    val targetDays: Int = 0b1111111,

    /**
     * Der Erstellungszeitpunkt der Gewohnheit.
     * Gespeichert als Millisekunden seit der Unix-Epoche (1. Januar 1970).
     */
    val createdAt: Long = System.currentTimeMillis(),

    /**
     * Die allgemeine Kategorie-Klassifizierung der Gewohnheit.
     * Standardmäßig "hobby". Diese Spalte wurde mit der Datenbankschema-Version 2 eingeführt.
     */
    val category: String = "hobby"
)