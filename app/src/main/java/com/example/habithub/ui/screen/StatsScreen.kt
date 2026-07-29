package com.example.habithub.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.habithub.R
import com.example.habithub.data.model.Habit
import com.example.habithub.data.model.HabitCompletion
import com.example.habithub.ui.component.WeeklyBarChart
import com.example.habithub.ui.theme.HabitHubTheme
import com.example.habithub.ui.viewmodel.HabitViewModel

/**
 * Ein zustandsbehafteter (stateful) Bildschirm zur Anzeige der Gewohnheitsstatistiken.
 * Beobachtet die relevanten Datenströme (StateFlows) aus dem [HabitViewModel], einschließlich
 * der definierten Gewohnheiten und deren Abschlusshistorie.
 *
 * Diese Komponente delegiert das eigentliche Rendering an [StatsScreenContent] und injiziert
 * die benötigten Berechnungsfunktionen direkt aus dem ViewModel, um die UI-Schicht von
 * komplexer Geschäftslogik freizuhalten.
 *
 * @param viewModel Das ViewModel zur Bereitstellung der Daten und Statistik-Berechnungen.
 */
@Composable
fun StatsScreen(viewModel: HabitViewModel) {
    val habits by viewModel.habits.collectAsState()
    val recentCompletions by viewModel.recentCompletions.collectAsState()
    val todayCompletions by viewModel.todayCompletions.collectAsState()

    StatsScreenContent(
        habits = habits,
        recentCompletions = recentCompletions,
        todayCompletions = todayCompletions,
        calculateStreak = { id, completions -> viewModel.calculateStreak(id, completions) },
        calculateBestStreak = { id, completions -> viewModel.calculateBestStreak(id, completions) },
        calculateCompletionRate = { habit, completions -> viewModel.calculateCompletionRate(habit, completions) },
        getWeeklyData = { id, completions -> viewModel.getWeeklyData(id, completions) }
    )
}

/**
 * Die zustandslose (stateless) UI-Kernkomponente für die Statistikansicht.
 * Nimmt die Rohdaten sowie Referenzen auf Berechnungsfunktionen (als Lambdas) entgegen und
 * generiert daraus eine aggregierte Übersichtskarte sowie detaillierte Statistik-Karten für
 * jede einzelne Gewohnheit.
 *
 * @param habits Die Liste aller angelegten Gewohnheiten.
 * @param recentCompletions Die Historie der Gewohnheitsabschlüsse (üblicherweise der letzten 90 Tage).
 * @param todayCompletions Die am heutigen Tag abgeschlossenen Gewohnheiten.
 * @param calculateStreak Funktion zur Ermittlung der aktuell laufenden Serie (Streak) einer Gewohnheit in Tagen.
 * @param calculateBestStreak Funktion zur Ermittlung der historisch längsten Serie einer Gewohnheit.
 * @param calculateCompletionRate Funktion zur Berechnung der Erfolgsquote (0.0f bis 1.0f).
 * @param getWeeklyData Funktion zur Extraktion der Abschlussdaten der letzten 7 Tage für das Balkendiagramm.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreenContent(
    habits: List<Habit>,
    recentCompletions: List<HabitCompletion>,
    todayCompletions: List<HabitCompletion>,
    calculateStreak: (Int, List<HabitCompletion>) -> Int,
    calculateBestStreak: (Int, List<HabitCompletion>) -> Int,
    calculateCompletionRate: (Habit, List<HabitCompletion>) -> Float,
    getWeeklyData: (Int, List<HabitCompletion>) -> List<Boolean>
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.stats_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        if (habits.isEmpty()) {
            // Leerer Zustand (Empty State) für Nutzer ohne angelegte Gewohnheiten
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    stringResource(R.string.stats_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            // Aggregation von globalen Metriken über alle Gewohnheiten hinweg
            val totalStreak = habits.sumOf { calculateStreak(it.id, recentCompletions) }
            val avgCompletionRate = habits
                .map { calculateCompletionRate(it, recentCompletions) }
                .average()
                .let { if (it.isNaN()) 0.0 else it }

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    OverallStatsCard(
                        totalHabits = habits.size,
                        completedToday = todayCompletions.size,
                        last90 = recentCompletions.size,
                        avgCompletionRate = avgCompletionRate.toFloat()
                    )
                }
                item {
                    Text(
                        stringResource(R.string.habit_details),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                items(habits, key = { it.id }) { habit ->
                    HabitStatCard(
                        habit = habit,
                        streak = calculateStreak(habit.id, recentCompletions),
                        bestStreak = calculateBestStreak(habit.id, recentCompletions),
                        completionRate = calculateCompletionRate(habit, recentCompletions),
                        weeklyData = getWeeklyData(habit.id, recentCompletions)
                    )
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

/**
 * UI-Komponente zur Darstellung der globalen App-Statistiken über alle Gewohnheiten hinweg.
 *
 * @param totalHabits Die Gesamtanzahl der vom Nutzer getrackten Gewohnheiten.
 * @param completedToday Anzahl der heute erledigten Gewohnheiten.
 * @param last90 Gesamtanzahl der Abschlüsse innerhalb des Analysezeitraums (z. B. 90 Tage).
 * @param avgCompletionRate Die durchschnittliche Erfolgsquote aller Gewohnheiten.
 */
@Composable
private fun OverallStatsCard(
    totalHabits: Int,
    completedToday: Int,
    last90: Int,
    avgCompletionRate: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(stringResource(R.string.overview), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatItem(value = totalHabits.toString(), label = stringResource(R.string.stat_habits))
                StatItem(value = completedToday.toString(), label = stringResource(R.string.stat_today))
                StatItem(value = last90.toString(), label = stringResource(R.string.stat_last_90))
                StatItem(value = "${(avgCompletionRate * 100).toInt()}%", label = stringResource(R.string.stat_avg_rate))
            }
        }
    }
}

/**
 * Hilfskomponente für die [OverallStatsCard] zur kompakten vertikalen Darstellung
 * eines einzelnen Datenpunktes (Wert und zugehörige Beschriftung).
 *
 * @param value Der darzustellende statistische Wert (z. B. "12" oder "85%").
 * @param label Die Beschreibung des Wertes.
 */
@Composable
private fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

/**
 * Detailkarte zur Visualisierung der Statistiken einer einzelnen, spezifischen Gewohnheit.
 * Enthält Basisinformationen (Icon, Name), Streaks, Abschlussrate und einen Verlaufs-Chart der letzten Woche.
 *
 * @param habit Das zugrundeliegende Gewohnheits-Modell.
 * @param streak Die aktuell fortlaufende Serie an Tagen.
 * @param bestStreak Der historische Rekord für fortlaufende Tage ohne Unterbrechung.
 * @param completionRate Die Erfolgsquote für diese Gewohnheit (Wert zwischen 0.0f und 1.0f).
 * @param weeklyData Eine Liste von 7 booleschen Werten, die den Status der letzten 7 Tage repräsentiert.
 */
@Composable
private fun HabitStatCard(
    habit: Habit,
    streak: Int,
    bestStreak: Int,
    completionRate: Float,
    weeklyData: List<Boolean>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Kopfbereich: Icon, Titel und aktuelle Serie
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(habit.colorValue)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(habit.emoji, fontSize = 22.sp)
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        habit.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        if (streak > 0) stringResource(R.string.streak_days_format, streak)
                        else stringResource(R.string.no_streak_yet),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (streak > 0) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // Hervorgehobene Anzeige der aktuellen Serie (Streak)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        streak.toString(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(stringResource(R.string.days), style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(Modifier.height(10.dp))

            // Sekundäre Statistiken (Rekord und Erfolgsquote)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MiniStatChip(
                    label = stringResource(R.string.best_streak),
                    value = "$bestStreak ${stringResource(R.string.days)}",
                    modifier = Modifier.weight(1f)
                )
                MiniStatChip(
                    label = stringResource(R.string.completion_rate),
                    value = "${(completionRate * 100).toInt()}%",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(14.dp))

            // Diagramm für den Wochenverlauf
            Text(
                stringResource(R.string.last_7_days),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            WeeklyBarChart(weeklyData = weeklyData, barColor = Color(habit.colorValue))
        }
    }
}

/**
 * Ein kompaktes UI-Element (Chip-Stil) zur Darstellung von Metadaten in der [HabitStatCard].
 *
 * @param label Der Titel der Metrik (z. B. "Beste Serie").
 * @param value Der formatierte Wert der Metrik.
 * @param modifier Erlaubt die externe Layout-Anpassung, insbesondere Gewichtung für gleichmäßige Breite.
 */
@Composable
private fun MiniStatChip(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Standard-Vorschau für den Statistikbildschirm im Android Studio Preview-Werkzeug.
 * Mockt die Berechnungslogik, um einen deterministischen Ansichtsstatus zu garantieren.
 */
@Preview(showBackground = true)
@Composable
fun StatsScreenPreview() {
    val habits = listOf(
        Habit(id = 1, name = "Reading", emoji = "📚", colorValue = 0xFF6750A4L),
        Habit(id = 2, name = "Workout", emoji = "💪", colorValue = 0xFF00897BL)
    )
    HabitHubTheme {
        StatsScreenContent(
            habits = habits,
            recentCompletions = emptyList(),
            todayCompletions = emptyList(),
            calculateStreak = { _, _ -> 3 },
            calculateBestStreak = { _, _ -> 7 },
            calculateCompletionRate = { _, _ -> 0.72f },
            getWeeklyData = { _, _ -> listOf(true, true, true, false, false, false, false) }
        )
    }
}