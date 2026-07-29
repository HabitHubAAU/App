package com.example.habithub.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.habithub.R
import com.example.habithub.data.model.Habit
import com.example.habithub.data.model.HabitCompletion
import com.example.habithub.ui.component.WeeklyBarChart
import com.example.habithub.ui.viewmodel.HabitViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Ein zustandsbehafteter (stateful) Wrapper-Bildschirm für die Detailansicht einer spezifischen Gewohnheit.
 * Diese Komponente extrahiert die benötigten Datenstrom-Zustände (StateFlows) aus dem [HabitViewModel],
 * berechnet die aggregierten Statistiken (Streaks, Abschlussraten, Historie) für die ausgewählte Gewohnheit
 * und delegiert die Darstellung an die zustandslose [DetailScreenContent]-Komponente.
 *
 * Falls die Gewohnheit nicht gefunden wird (z. B. nach einer Löschung), wird automatisch
 * eine Navigation zurück ausgelöst.
 *
 * @param habitId Die eindeutige Datenbank-ID der anzuzeigenden Gewohnheit.
 * @param viewModel Das ViewModel, welches die Daten-Streams und Berechnungslogik bereitstellt.
 * @param onNavigateBack Ein Callback zur Navigation zum vorherigen Bildschirm.
 * @param onEditHabit Ein Callback, der den Navigationspfad zum Bearbeitungsbildschirm für diese Gewohnheit aufruft.
 */
@Composable
fun DetailScreen(
    habitId: Int,
    viewModel: HabitViewModel,
    onNavigateBack: () -> Unit,
    onEditHabit: (Int) -> Unit
) {
    val habits by viewModel.habits.collectAsState()
    val recentCompletions by viewModel.recentCompletions.collectAsState()
    val habit = habits.firstOrNull { it.id == habitId }

    if (habit == null) {
        LaunchedEffect(Unit) { onNavigateBack() }
        return
    }

    DetailScreenContent(
        habit = habit,
        streak = viewModel.calculateStreak(habitId, recentCompletions),
        bestStreak = viewModel.calculateBestStreak(habitId, recentCompletions),
        completionRate = viewModel.calculateCompletionRate(habit, recentCompletions),
        weeklyData = viewModel.getWeeklyData(habitId, recentCompletions),
        monthlyData = viewModel.get30DayData(habitId, recentCompletions),
        totalCompletions = recentCompletions.count { it.habitId == habitId },
        onNavigateBack = onNavigateBack,
        onEditHabit = { onEditHabit(habitId) }
    )
}

/**
 * Die zustandslose (stateless) UI-Kernkomponente für die Darstellung der Gewohnheitsdetails.
 * Baut das Layout mittels Scaffold und LazyColumn auf und integriert verschiedene
 * Visualisierungs-Karten für den Status und die Historie.
 *
 * @param habit Das Datenmodell der darzustellenden Gewohnheit.
 * @param streak Die aktuelle ununterbrochene Erfolgsserie in Tagen.
 * @param bestStreak Die historisch längste ununterbrochene Erfolgsserie.
 * @param completionRate Die prozentuale Abschlussrate (Wert zwischen 0.0 und 1.0).
 * @param weeklyData Eine chronologische Liste der letzten 7 Tage als Boolean (true = abgeschlossen).
 * @param monthlyData Eine chronologische Liste der letzten 30 Tage als Boolean (true = abgeschlossen).
 * @param totalCompletions Die absolute Anzahl aller bisherigen Abschlüsse dieser Gewohnheit.
 * @param onNavigateBack Callback zur Auslösung der Zurück-Navigation.
 * @param onEditHabit Callback zur Aktivierung des Bearbeitungsmodus.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreenContent(
    habit: Habit,
    streak: Int,
    bestStreak: Int,
    completionRate: Float,
    weeklyData: List<Boolean>,
    monthlyData: List<Boolean>,
    totalCompletions: Int,
    onNavigateBack: () -> Unit,
    onEditHabit: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(habit.name, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = onEditHabit) {
                        Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.cd_edit_habit))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { HabitHeaderCard(habit) }
            item {
                StatsOverviewRow(
                    streak = streak,
                    bestStreak = bestStreak,
                    completionRate = completionRate,
                    totalCompletions = totalCompletions
                )
            }
            item { MonthlyHeatmapCard(monthlyData, Color(habit.colorValue)) }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            stringResource(R.string.last_7_days),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(12.dp))
                        WeeklyBarChart(
                            weeklyData = weeklyData,
                            barColor = Color(habit.colorValue),
                            barWidth = 36.dp,
                            barHeight = 56.dp,
                            labelFontSize = 10.sp
                        )
                    }
                }
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

/**
 * UI-Komponente für den oberen Informationsbereich der Detailansicht.
 * Visualisiert primäre Metadaten der Gewohnheit: Farbe, Emoji, Titel, Beschreibung und Erstelldatum.
 *
 * @param habit Die darzustellende Gewohnheit.
 */
@Composable
private fun HabitHeaderCard(habit: Habit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color(habit.colorValue)),
                contentAlignment = Alignment.Center
            ) {
                Text(habit.emoji, fontSize = 34.sp)
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    habit.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (habit.description.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        habit.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                }
                Spacer(Modifier.height(6.dp))
                val since = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                    .format(Date(habit.createdAt))
                Text(
                    stringResource(R.string.since_format, since),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
                )
            }
        }
    }
}

/**
 * Stellt eine horizontale Reihe (Row) von vier gleich großen Statistik-Karten dar,
 * die einen schnellen Überblick über die Gesamt-Performance der Gewohnheit geben.
 *
 * @param streak Die aktuelle Erfolgsserie.
 * @param bestStreak Die längste historische Erfolgsserie.
 * @param completionRate Die Erfolgsquote (wird intern mit 100 multipliziert zur Prozentdarstellung).
 * @param totalCompletions Die Gesamtanzahl der erfolgreichen Durchführungen.
 */
@Composable
private fun StatsOverviewRow(
    streak: Int,
    bestStreak: Int,
    completionRate: Float,
    totalCompletions: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        DetailStatCard("🔥", "$streak", stringResource(R.string.current_streak), Modifier.weight(1f))
        DetailStatCard("🏆", "$bestStreak", stringResource(R.string.best_streak), Modifier.weight(1f))
        DetailStatCard("✅", "${(completionRate * 100).toInt()}%", stringResource(R.string.rate), Modifier.weight(1f))
        DetailStatCard("📊", "$totalCompletions", stringResource(R.string.total), Modifier.weight(1f))
    }
}

/**
 * Eine generische, wiederverwendbare UI-Komponente zur Darstellung eines einzelnen statistischen Wertes.
 *
 * @param icon Das Emoji zur visuellen Repräsentation der Statistik.
 * @param value Der formatierte numerische Wert als String (z. B. "12" oder "85%").
 * @param label Die Textbeschreibung der Metrik.
 * @param modifier Der Modifier, welcher typischerweise ein Row-Weight für gleichmäßige Verteilung vorgibt.
 */
@Composable
private fun DetailStatCard(icon: String, value: String, label: String, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(icon, fontSize = 20.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Zeigt die Historie der letzten 30 Tage in einem visuellen Raster-Layout (Heatmap) an.
 * Die Daten werden in 6 Reihen mit jeweils 5 Spalten (Tagen) unterteilt,
 * wobei das älteste Datum oben links und das aktuelle Datum unten rechts abgebildet wird.
 *
 * @param data Eine exakt 30 Elemente umfassende Liste mit den Abschluss-Status der letzten Tage.
 * @param completedColor Die spezifische Theme-Farbe der Gewohnheit zur Markierung erfolgreicher Tage.
 */
@Composable
private fun MonthlyHeatmapCard(data: List<Boolean>, completedColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                stringResource(R.string.last_30_days),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(12.dp))
            // 6 Reihen × 5 Spalten = 30 Tage, der älteste Tag ist oben links
            data.chunked(5).forEach { rowData ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    rowData.forEach { completed ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (completed) completedColor
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
            }
            Spacer(Modifier.height(4.dp))
            // Legende zur farblichen Erklärung der Heatmap
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
                Spacer(Modifier.width(4.dp))
                Text(stringResource(R.string.missed), style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(completedColor)
                )
                Spacer(Modifier.width(4.dp))
                Text(stringResource(R.string.cd_done), style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}