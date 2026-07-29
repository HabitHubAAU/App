package com.example.habithub.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Eine versiegelte Klasse (Sealed Class), die alle verfügbaren Bildschirme (Destinations)
 * innerhalb der Jetpack Compose Navigation der Applikation definiert.
 * Sorgt für ein typsicheres Routing und eine zentrale Verwaltung der Navigationspfade,
 * UI-Titel und zugehörigen Icons für beispielsweise eine Bottom Navigation Bar.
 *
 * @property route Der eindeutige Pfad/String, der vom Compose NavController verwendet wird.
 * @property label Der sichtbare Titel des Bildschirms, z. B. für Beschriftungen in der Navigation.
 * @property icon Das Vektor-Icon, das diesen Bildschirm visuell in der UI repräsentiert.
 */
sealed class Screen(val route: String, val label: String, val icon: ImageVector) {

    /** Hauptbildschirm, der die aktuellen Gewohnheiten des heutigen Tages auflistet. */
    object Home   : Screen("home",             "Today",  Icons.Filled.Home)

    /** Bildschirm zum Erstellen und Hinzufügen einer neuen Gewohnheit. */
    object Add    : Screen("add",              "Add",    Icons.Filled.Add)

    /** Übersichtsbildschirm für statistische Auswertungen des Gewohnheitsfortschritts. */
    object Stats  : Screen("stats",            "Stats",  Icons.Filled.BarChart)

    /**
     * Detailansicht einer spezifischen Gewohnheit.
     * Enthält einen dynamischen Parameter `{habitId}` im Navigationspfad.
     */
    object Detail : Screen("detail/{habitId}", "Detail", Icons.Filled.Info) {
        /**
         * Generiert den konkreten, zur Laufzeit navigierbaren Pfad zur Detailansicht.
         *
         * @param id Die eindeutige Datenbank-ID der anzuzeigenden Gewohnheit.
         * @return Der formatierte Route-String (z. B. "detail/42").
         */
        fun route(id: Int) = "detail/$id"
    }

    /**
     * Bearbeitungsansicht für eine spezifische Gewohnheit.
     * Enthält einen dynamischen Parameter `{habitId}` im Navigationspfad.
     */
    object Edit   : Screen("edit/{habitId}",   "Edit",   Icons.Filled.Edit) {
        /**
         * Generiert den konkreten, zur Laufzeit navigierbaren Pfad zur Bearbeitungsansicht.
         *
         * @param id Die eindeutige Datenbank-ID der zu bearbeitenden Gewohnheit.
         * @return Der formatierte Route-String (z. B. "edit/42").
         */
        fun route(id: Int) = "edit/$id"
    }

    /** Bildschirm zur Erfassung von Vitaldaten wie dem Puls während spezieller Gewohnheiten. */
    object Pulse  : Screen("pulse",            "Puls",   Icons.Filled.Info)

    /** Bildschirm, der einen Pomodoro-Timer zur fokussierten Ausführung einer Gewohnheit anzeigt. */
    object Pomodoro : Screen("pomodoro",       "Pomodoro", Icons.Filled.Info)

    /** Einstellungsbildschirm für die Konfiguration genereller App-Präferenzen und Sensoren. */
    object Settings : Screen("settings",       "Settings", Icons.Filled.Settings)
}