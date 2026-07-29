package com.example.habithub.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.habithub.data.model.Habit
import com.example.habithub.data.model.HabitCompletion

/**
 * Deklariert die zentrale Room-Datenbank der App.
 * Definiert die verknüpften Entitäten (Tabellen) und legt die aktuelle Versionsnummer der Datenbank fest.
 * exportSchema = false verhindert die Generierung einer JSON-Datei mit dem Datenbankschema.
 */
@Database(
    entities = [Habit::class, HabitCompletion::class],
    version = 2,
    exportSchema = false
)
abstract class HabitDatabase : RoomDatabase() {

    /**
     * Stellt den Zugriff auf die Datenbankoperationen für die "habits"-Tabelle bereit.
     */
    abstract fun habitDao(): HabitDao

    /**
     * Stellt den Zugriff auf die Datenbankoperationen für die "habit_completions"-Tabelle bereit.
     */
    abstract fun completionDao(): HabitCompletionDao

    companion object {
        /**
         * Hält die einzige Instanz der Datenbank als Singleton.
         * Die @Volatile-Annotation garantiert, dass Schreibzugriffe auf diese Variable sofort für alle Threads sichtbar sind.
         */
        @Volatile
        private var INSTANCE: HabitDatabase? = null

        /**
         * Definiert die Migrationslogik beim Upgrade der Datenbank von Version 1 auf Version 2.
         * Führt einen SQL-Befehl aus, um die bestehende "habits"-Tabelle um die Spalte "category" zu erweitern.
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE habits ADD COLUMN category TEXT NOT NULL DEFAULT 'hobby'")
            }
        }

        /**
         * Gibt die vorhandene Datenbankinstanz zurück oder erstellt eine neue, falls noch keine existiert.
         * Nutzt einen synchronisierten Block (Double-Check-Locking), um zu verhindern,
         * dass bei gleichzeitigen Zugriffen aus mehreren Threads versehentlich mehrere Instanzen erstellt werden.
         */
        fun getDatabase(context: Context): HabitDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    HabitDatabase::class.java,
                    "habit_database"
                )
                    .addMigrations(MIGRATION_1_2) // Integriert die Migration in den Build-Prozess
                    .build().also { INSTANCE = it }
            }
        }
    }
}