package com.habithub.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.habithub.data.model.Habit
import com.habithub.ui.viewmodel.HabitViewModel

private val EDIT_PRESET_EMOJIS = listOf(
    "⭐", "💪", "🏃", "📚", "💧", "🧘", "🎯", "🌙", "☀️",
    "🍎", "✍️", "🎵", "💊", "🧹", "💻", "🌿", "🔥", "❤️", "🎨", "🏋️"
)

private val EDIT_PRESET_COLORS = listOf(
    0xFF6750A4L, 0xFF00897BL, 0xFFE53935L, 0xFF43A047L,
    0xFF1E88E5L, 0xFFFB8C00L, 0xFFD81B60L, 0xFF546E7AL
)

private val EDIT_DAY_LABELS = listOf("Mo", "Di", "Mi", "Do", "Fr", "Sa", "So")

@Composable
fun EditHabitScreen(
    habitId: Int,
    viewModel: HabitViewModel,
    onNavigateBack: () -> Unit,
    onDeleteHabit: (Habit) -> Unit
) {
    val habits by viewModel.habits.collectAsState()
    val habit = habits.firstOrNull { it.id == habitId }

    if (habit == null) {
        LaunchedEffect(Unit) { onNavigateBack() }
        return
    }

    EditHabitScreenContent(
        habit = habit,
        onSaveHabit = { name, description, emoji, color, days ->
            viewModel.updateHabit(
                habit.copy(
                    name = name.trim(),
                    description = description.trim(),
                    emoji = emoji,
                    colorValue = color,
                    targetDays = days
                )
            )
        },
        onDeleteHabit = {
            onDeleteHabit(habit)
        },
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditHabitScreenContent(
    habit: Habit,
    onSaveHabit: (String, String, String, Long, Int) -> Unit,
    onDeleteHabit: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var name by remember { mutableStateOf(habit.name) }
    var description by remember { mutableStateOf(habit.description) }
    var selectedEmoji by remember { mutableStateOf(habit.emoji) }
    var selectedColor by remember { mutableLongStateOf(habit.colorValue) }
    var selectedDays by remember { mutableIntStateOf(habit.targetDays) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete habit?") },
            text = { Text("\"${habit.name}\" and all its history will be permanently deleted.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteHabit()
                        onNavigateBack()
                    }
                ) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Habit") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Delete habit",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Habit name *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (optional)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2,
                shape = RoundedCornerShape(12.dp)
            )

            // Emoji picker
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Icon", style = MaterialTheme.typography.labelLarge)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(EDIT_PRESET_EMOJIS) { emoji ->
                        val selected = emoji == selectedEmoji
                        Surface(
                            onClick = { selectedEmoji = emoji },
                            shape = CircleShape,
                            color = if (selected) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(52.dp),
                            border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                        ) {
                            Box(contentAlignment = Alignment.Center) { Text(emoji, fontSize = 26.sp) }
                        }
                    }
                }
            }

            // Color picker
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Color", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    EDIT_PRESET_COLORS.forEach { color ->
                        val selected = color == selectedColor
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(color))
                                .then(
                                    if (selected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                    else Modifier
                                )
                                .clickable { selectedColor = color }
                        ) {
                            if (selected) {
                                Icon(
                                    Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.align(Alignment.Center).size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Day selector
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Repeat on", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    EDIT_DAY_LABELS.forEachIndexed { index, label ->
                        val isSelected = (selectedDays and (1 shl index)) != 0
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedDays = if (isSelected)
                                    selectedDays and (1 shl index).inv()
                                else
                                    selectedDays or (1 shl index)
                            },
                            label = { Text(label, fontSize = 12.sp) }
                        )
                    }
                }
            }

            // Live preview
            if (name.isNotBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(52.dp).clip(CircleShape).background(Color(selectedColor)),
                            contentAlignment = Alignment.Center
                        ) { Text(selectedEmoji, fontSize = 26.sp) }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(name, style = MaterialTheme.typography.titleMedium)
                            if (description.isNotBlank()) {
                                Text(description, style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSaveHabit(name, description, selectedEmoji, selectedColor, selectedDays)
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = name.isNotBlank(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Check, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Save Changes", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
