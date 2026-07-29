package com.example.habithub.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.habithub.R
import java.util.Calendar

/**
 * Eine visuelle Jetpack Compose-Komponente, die ein wöchentliches Balkendiagramm darstellt.
 * Dient primär der Visualisierung des Abschlussstatus einer Gewohnheit über die letzten sieben Tage.
 *
 * Das Diagramm berechnet die Labels für die Wochentage dynamisch rückwirkend vom heutigen Datum.
 * Der äußerste rechte Balken repräsentiert immer "heute".
 *
 * @param weeklyData Eine Liste von Wahrheitswerten (Booleans), die den Abschlussstatus der einzelnen
 *                   Tage repräsentiert (z. B. `true` für abgeschlossen). Die Liste sollte chronologisch
 *                   von alt (Index 0) bis neu (letzter Index) geordnet sein und idealerweise 7 Elemente umfassen.
 * @param barColor Die Füllfarbe, die für erfolgreich abgeschlossene Tage (`true`) verwendet wird.
 *                 Tage mit dem Wert `false` erhalten automatisch eine unauffällige Systemfarbe (`surfaceVariant`).
 * @param modifier Ein optionaler [Modifier] zur Anpassung des äußeren Row-Layouts.
 * @param barWidth Die festgelegte Breite eines einzelnen Tages-Balkens.
 * @param barHeight Die festgelegte maximale Höhe eines einzelnen Tages-Balkens.
 * @param labelFontSize Die Schriftgröße der dynamisch generierten Wochentags-Labels unterhalb der Balken.
 */
@Composable
fun WeeklyBarChart(
    weeklyData: List<Boolean>,
    barColor: Color,
    modifier: Modifier = Modifier,
    barWidth: Dp = 30.dp,
    barHeight: Dp = 44.dp,
    labelFontSize: TextUnit = 9.sp
) {
    // Lädt die lokalisierten Kurznamen der Wochentage (z. B. "Mo", "Di", "Mi") aus den Ressourcen.
    // Es wird erwartet, dass Sonntag auf Index 0 oder 1 liegt, abhängig von der Calendar-Implementierung.
    val dayNames = stringArrayResource(R.array.weekday_short)

    // Berechnet die korrekten Wochentags-Labels für die letzten 7 Tage (inklusive heute).
    // Die Schleife zählt von 6 Tagen in der Vergangenheit herunter bis zu 0 (heute).
    val labels = (6 downTo 0).map { daysAgo ->
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
        // Calendar.DAY_OF_WEEK liefert Werte von 1 (Sonntag) bis 7 (Samstag).
        // Um auf das Array zuzugreifen, muss 1 abgezogen werden.
        dayNames[cal.get(Calendar.DAY_OF_WEEK) - 1]
    }

    // Das Haupt-Layout, das die einzelnen Balken horizontal nebeneinander platziert.
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // Iteriert durch die übergebenen Statusdaten und generiert für jeden Tag eine Spalte.
        weeklyData.forEachIndexed { index, completed ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp) // Abstand zwischen Balken und Text
            ) {
                // Der eigentliche Balken (Box), dessen Farbe je nach Abschlussstatus gesetzt wird.
                Box(
                    modifier = Modifier
                        .width(barWidth)
                        .height(barHeight)
                        .clip(RoundedCornerShape(6.dp)) // Leicht abgerundete Ecken für eine moderne Optik
                        .background(
                            if (completed) barColor
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                )
                // Das zugehörige Wochentags-Label. Fällt zurück auf einen leeren String,
                // falls 'weeklyData' mehr Elemente enthält als Labels berechnet wurden.
                Text(
                    text = labels.getOrElse(index) { "" },
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = labelFontSize,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}