package com.example.habithub.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.habithub.data.model.Habit
import com.example.habithub.ui.theme.HabitHubTheme
import com.example.habithub.ui.viewmodel.HabitViewModel
import com.example.habithub.ui.viewmodel.SortOrder
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    viewModel: HabitViewModel,
    onHabitClick: (Habit) -> Unit,
    onHabitLongClick: (Habit) -> Unit,
    stepCount: Int?,
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {}
) {
    val habits by viewModel.sortedHabits.collectAsState()
    val todayCompletions by viewModel.todayCompletions.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()

    HomeScreenContent(
        habits = habits,
        completedIds = remember(todayCompletions) { todayCompletions.map { it.habitId }.toSet() },
        onToggleCompletion = { viewModel.toggleCompletion(it) },
        onDeleteHabit = { viewModel.deleteHabit(it) },
        onHabitClick = onHabitClick,
        onHabitLongClick = onHabitLongClick,
        sortOrder = sortOrder,
        onSortOrderChange = { viewModel.setSortOrder(it) },
        stepCount = stepCount,
        isDarkTheme = isDarkTheme,
        onToggleTheme = onToggleTheme
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    habits: List<Habit>,
    completedIds: Set<Int>,
    onToggleCompletion: (Habit) -> Unit,
    onDeleteHabit: (Habit) -> Unit,
    onHabitClick: (Habit) -> Unit = {},
    onHabitLongClick: (Habit) -> Unit = {},
    sortOrder: SortOrder = SortOrder.DEFAULT,
    onSortOrderChange: (SortOrder) -> Unit = {},
    stepCount: Int? = null,
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {}
) {
    val dateLabel = remember {
        SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date())
    }
    val greeting = remember {
        when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 5..11  -> "Good morning"
            in 12..17 -> "Good afternoon"
            else      -> "Good evening"
        }
    }
    var showSortMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(greeting, style = MaterialTheme.typography.titleLarge)
                        Text(
                            dateLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onToggleTheme) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                            contentDescription = "Toggle theme",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(
                                Icons.Filled.Sort,
                                contentDescription = "Sort habits",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            SortOrder.entries.forEach { order ->
                                DropdownMenuItem(
                                    text = { Text(order.label) },
                                    onClick = {
                                        onSortOrderChange(order)
                                        showSortMenu = false
                                    },
                                    leadingIcon = if (sortOrder == order) {
                                        { Icon(Icons.Filled.Check, contentDescription = null) }
                                    } else null
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (habits.isNotEmpty()) {
                item {
                    ProgressCard(completed = completedIds.size, total = habits.size)
                }
            }
            item {
                StepCountCard(steps = stepCount ?: 0)
            }
            if (habits.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No habits yet!", style = MaterialTheme.typography.headlineSmall)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Tap Add to create your first habit.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                item {
                    Text(
                        "Tap for details  •  Long-press to edit  •  Swipe right ✓  •  Swipe left 🗑",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
                items(habits, key = { it.id }) { habit ->
                    HabitCard(
                        habit = habit,
                        isCompleted = habit.id in completedIds,
                        onComplete = { onToggleCompletion(habit) },
                        onDelete = { onDeleteHabit(habit) },
                        onCardClick = { onHabitClick(habit) },
                        onCardLongClick = { onHabitLongClick(habit) },
                        modifier = Modifier.animateItem()
                    )
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun ProgressCard(completed: Int, total: Int) {
    val progress = if (total == 0) 0f else completed.toFloat() / total.toFloat()
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Today's Progress", style = MaterialTheme.typography.titleMedium)
                Text(
                    "$completed / $total",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
            )
            if (completed == total && total > 0) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "All done for today! 🎉",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun StepCountCard(steps: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.DirectionsWalk,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    "Steps since launch",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                )
                Text(
                    steps.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitCard(
    habit: Habit,
    isCompleted: Boolean,
    onComplete: () -> Unit,
    onDelete: () -> Unit,
    onCardClick: () -> Unit = {},
    onCardLongClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val currentOnComplete by rememberUpdatedState(onComplete)
    val currentOnDelete by rememberUpdatedState(onDelete)

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> { currentOnComplete(); false }
                SwipeToDismissBoxValue.EndToStart -> { currentOnDelete(); true }
                else -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        backgroundContent = { SwipeBackground(dismissState) }
    ) {
        HabitCardContent(
            habit = habit,
            isCompleted = isCompleted,
            onComplete = onComplete,
            onCardClick = onCardClick,
            onCardLongClick = onCardLongClick
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeBackground(state: SwipeToDismissBoxState) {
    val color by animateColorAsState(
        targetValue = when (state.targetValue) {
            SwipeToDismissBoxValue.StartToEnd -> Color(0xFF4CAF50)
            SwipeToDismissBoxValue.EndToStart -> Color(0xFFF44336)
            else -> Color.Transparent
        },
        label = "swipeBg"
    )
    val alignment = if (state.targetValue == SwipeToDismissBoxValue.StartToEnd)
        Alignment.CenterStart else Alignment.CenterEnd
    val icon = when (state.targetValue) {
        SwipeToDismissBoxValue.StartToEnd -> Icons.Filled.Check
        SwipeToDismissBoxValue.EndToStart -> Icons.Filled.Delete
        else -> null
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(color)
            .padding(horizontal = 24.dp),
        contentAlignment = alignment
    ) {
        icon?.let {
            Icon(it, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HabitCardContent(
    habit: Habit,
    isCompleted: Boolean,
    onComplete: () -> Unit,
    onCardClick: () -> Unit,
    onCardLongClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isCompleted)
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
        else
            MaterialTheme.colorScheme.surface,
        label = "cardBg"
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onCardClick, onLongClick = onCardLongClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color(habit.colorValue)),
                contentAlignment = Alignment.Center
            ) {
                Text(habit.emoji, fontSize = 26.sp)
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isCompleted) FontWeight.Normal else FontWeight.SemiBold,
                    textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (habit.description.isNotBlank()) {
                    Text(
                        text = habit.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            IconButton(onClick = onComplete) {
                AnimatedContent(
                    targetState = isCompleted,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "checkAnim"
                ) { done ->
                    if (done) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = "Done",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    } else {
                        Icon(
                            Icons.Outlined.RadioButtonUnchecked,
                            contentDescription = "Mark done",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    val habits = listOf(
        Habit(id = 1, name = "Reading", emoji = "📚", colorValue = 0xFF6750A4L),
        Habit(id = 2, name = "Workout", emoji = "💪", colorValue = 0xFF00897BL)
    )
    HabitHubTheme {
        HomeScreenContent(
            habits = habits,
            completedIds = setOf(1),
            onToggleCompletion = {},
            onDeleteHabit = {},
            stepCount = 342
        )
    }
}