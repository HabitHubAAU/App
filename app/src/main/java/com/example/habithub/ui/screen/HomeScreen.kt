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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Favorite
import com.example.habithub.R
import com.example.habithub.data.model.Habit
import com.example.habithub.ui.theme.HabitHubTheme
import com.example.habithub.ui.viewmodel.HabitViewModel
import com.example.habithub.ui.viewmodel.SortOrder
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.runtime.mutableIntStateOf

@Composable
fun HomeScreen(
    viewModel: HabitViewModel,
    onHabitClick: (Habit) -> Unit,
    onHabitLongClick: (Habit) -> Unit,
    stepCount: Int?,
    onSettingsClick: () -> Unit = {},
    onPulseClick: () -> Unit = {},
    onPomodoroClick: () -> Unit = {}
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
        onSettingsClick = onSettingsClick,
        onPulseClick = onPulseClick,
        onPomodoroClick = onPomodoroClick
    )
}

private val HOME_TAB_RES = listOf(R.string.tab_all, R.string.tab_study, R.string.tab_hobby, R.string.tab_work)
private val HOME_CATEGORIES = listOf(null, "study", "hobby", "work")

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
    onSettingsClick: () -> Unit = {},
    onPulseClick: () -> Unit = {},
    onPomodoroClick: () -> Unit = {}
) {
    val dateLabel = remember {
        SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date())
    }
    val greetingRes = remember {
        when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 5..11  -> R.string.greeting_morning
            in 12..17 -> R.string.greeting_afternoon
            else      -> R.string.greeting_evening
        }
    }
    val greeting = stringResource(greetingRes)
    var showSortMenu by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }

    val filteredHabits = remember(habits, selectedTab) {
        val cat = HOME_CATEGORIES[selectedTab]
        if (cat == null) habits else habits.filter { it.category == cat }
    }
    val filteredCompletedIds = remember(completedIds, filteredHabits) {
        completedIds.intersect(filteredHabits.map { it.id }.toSet())
    }

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
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = stringResource(R.string.action_settings),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(
                                Icons.Filled.Sort,
                                contentDescription = stringResource(R.string.action_sort),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            SortOrder.entries.forEach { order ->
                                DropdownMenuItem(
                                    text = { Text(stringResource(order.labelRes)) },
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
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                HOME_TAB_RES.forEachIndexed { index, titleRes ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(stringResource(titleRes)) }
                    )
                }
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (filteredHabits.isNotEmpty()) {
                    item {
                        ProgressCard(completed = filteredCompletedIds.size, total = filteredHabits.size)
                    }
                }
                item {
                    StepCountCard(steps = stepCount ?: 0)
                }
                if (selectedTab == 1) {
                    item { PomodoroCard(onPomodoroClick = onPomodoroClick) }
                }
                if (selectedTab == 2) {
                    item { PulseCard(onPulseClick = onPulseClick) }
                }
                if (filteredHabits.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(stringResource(R.string.no_habits_title), style = MaterialTheme.typography.headlineSmall)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                stringResource(R.string.no_habits_subtitle),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    item {
                        Text(
                            stringResource(R.string.home_hint),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                    items(filteredHabits, key = { it.id }) { habit ->
                        HabitCard(
                            habit = habit,
                            isCompleted = habit.id in filteredCompletedIds,
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
                Text(stringResource(R.string.todays_progress), style = MaterialTheme.typography.titleMedium)
                Text(
                    stringResource(R.string.progress_count, completed, total),
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
                    stringResource(R.string.all_done_today),
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
                    stringResource(R.string.steps_since_launch),
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
                            contentDescription = stringResource(R.string.cd_done),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    } else {
                        Icon(
                            Icons.Outlined.RadioButtonUnchecked,
                            contentDescription = stringResource(R.string.cd_mark_done),
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PomodoroCard(onPomodoroClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🍅", fontSize = 28.sp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.pomodoro_card_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Text(
                    stringResource(R.string.pomodoro_card_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                )
            }
            Button(
                onClick = onPomodoroClick,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
            ) {
                Text(stringResource(R.string.start))
            }
        }
    }
}

@Composable
private fun PulseCard(onPulseClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Favorite,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.pulse_card_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    stringResource(R.string.pulse_card_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                )
            }
            Button(
                onClick = onPulseClick,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(stringResource(R.string.start))
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