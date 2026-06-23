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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.habithub.data.model.Habit
import com.example.habithub.data.model.HabitCompletion
import com.example.habithub.ui.viewmodel.HabitViewModel
import java.text.SimpleDateFormat
import java.util.*

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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onEditHabit) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit habit")
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
                            "Last 7 days",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(12.dp))
                        WeeklyBarChartDetail(weeklyData, Color(habit.colorValue))
                    }
                }
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

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
                    "Since $since",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
                )
            }
        }
    }
}

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
        DetailStatCard("🔥", "$streak", "Current streak", Modifier.weight(1f))
        DetailStatCard("🏆", "$bestStreak", "Best streak", Modifier.weight(1f))
        DetailStatCard("✅", "${(completionRate * 100).toInt()}%", "Rate", Modifier.weight(1f))
        DetailStatCard("📊", "$totalCompletions", "Total", Modifier.weight(1f))
    }
}

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

@Composable
private fun MonthlyHeatmapCard(data: List<Boolean>, completedColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Last 30 days",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(12.dp))
            // 6 rows × 5 columns = 30 days, oldest top-left
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
                Text("Missed", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(completedColor)
                )
                Spacer(Modifier.width(4.dp))
                Text("Done", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun WeeklyBarChartDetail(weeklyData: List<Boolean>, barColor: Color) {
    val dayNames = arrayOf("", "So", "Mo", "Di", "Mi", "Do", "Fr", "Sa")
    val labels = (6 downTo 0).map { daysAgo ->
        val cal = java.util.Calendar.getInstance()
        cal.add(java.util.Calendar.DAY_OF_YEAR, -daysAgo)
        dayNames[cal.get(java.util.Calendar.DAY_OF_WEEK)]
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        weeklyData.forEachIndexed { index, completed ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(56.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (completed) barColor
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                )
                Text(
                    text = labels.getOrElse(index) { "" },
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
