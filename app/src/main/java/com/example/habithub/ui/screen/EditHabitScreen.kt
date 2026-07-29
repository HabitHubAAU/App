package com.example.habithub.ui.screen

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
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.habithub.R
import com.example.habithub.data.model.Habit
import com.example.habithub.ui.viewmodel.HabitViewModel

/**
 * Eine vordefinierte Liste von Emojis zur visuellen Anpassung der Gewohnheit im Bearbeitungsmodus.
 */
private val EDIT_PRESET_EMOJIS = listOf(
    "⭐", "💪", "🏃", "📚", "💧", "🧘", "🎯", "🌙", "☀️",
    "🍎", "✍️", "🎵", "💊", "🧹", "💻", "🌿", "🔥", "❤️", "🎨", "🏋️"
)

/**
 * Eine vordefinierte Liste von ARGB-Farbwerten (als Long) zur farblichen Anpassung der Gewohnheit.
 */
private val EDIT_PRESET_COLORS = listOf(
    0xFF6750A4L, 0xFF00897BL, 0xFFE53935L, 0xFF43A047L,
    0xFF1E88E5L, 0xFFFB8C00L, 0xFFD81B60L, 0xFF546E7AL
)

/**
 * Ein zustandsbehafteter (stateful) Wrapper-Bildschirm für die Bearbeitung einer bestehenden Gewohnheit.
 * Diese Komponente liest die aktuelle Gewohnheit anhand der übergebenen [habitId] aus dem [HabitViewModel] aus.
 * Sie delegiert die Aktualisierung oder Löschung der Daten an die entsprechenden ViewModel-Funktionen.
 *
 * Falls die gesuchte Gewohnheit nicht (mehr) in der Liste gefunden wird,
 * wird sicherheitshalber eine automatische Navigation zurück ausgelöst.
 *
 * @param habitId Die eindeutige Datenbank-ID der zu bearbeitenden Gewohnheit.
 * @param viewModel Das ViewModel zur Verwaltung und Aktualisierung der Gewohnheitsdaten in der Datenbank.
 * @param onNavigateBack Ein Callback zur Rückkehr zum vorherigen Bildschirm (z. B. nach Speichern oder Abbrechen).
 * @param onDeleteHabit Ein Callback, der aufgerufen wird, wenn die Gewohnheit erfolgreich gelöscht werden soll.
 */
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
        onSaveHabit = { name, description, emoji, color, days, category ->
            viewModel.updateHabit(
                habit.copy(
                    name = name.trim(),
                    description = description.trim(),
                    emoji = emoji,
                    colorValue = color,
                    targetDays = days,
                    category = category
                )
            )
        },
        onDeleteHabit = {
            onDeleteHabit(habit)
        },
        onNavigateBack = onNavigateBack
    )
}

/**
 * Eine vordefinierte Liste von verfügbaren Kategorien für den Bearbeitungsmodus,
 * bestehend aus einem internen Schlüssel und der zugehörigen String-Ressourcen-ID.
 */
private val EDIT_CATEGORIES = listOf(
    "hobby" to R.string.category_hobby,
    "study" to R.string.category_study,
    "work" to R.string.category_work
)

/**
 * Die UI-Kernkomponente für das Formular zur Bearbeitung einer Gewohnheit.
 * Initialisiert die lokalen Zustände der Eingabefelder mit den Werten der übergebenen [habit].
 * Bietet zusätzlich einen Bestätigungsdialog für das Löschen der Gewohnheit, um versehentlichen Datenverlust zu vermeiden.
 *
 * @param habit Das Datenmodell der Gewohnheit mit den initialen Werten für das Formular.
 * @param onSaveHabit Ein Callback, der beim Klick auf "Speichern" die aktualisierten Formulardaten übergibt.
 * @param onDeleteHabit Ein Callback zur Bestätigung der Löschung der Gewohnheit.
 * @param onNavigateBack Ein Callback zur Navigation, ausgelöst durch den Zurück-Pfeil oder nach Abschluss einer Aktion.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditHabitScreenContent(
    habit: Habit,
    onSaveHabit: (String, String, String, Long, Int, String) -> Unit,
    onDeleteHabit: () -> Unit,
    onNavigateBack: () -> Unit
) {
    // Lokale Zustände, initialisiert mit den bestehenden Daten der Gewohnheit
    var name by remember { mutableStateOf(habit.name) }
    var description by remember { mutableStateOf(habit.description) }
    var selectedEmoji by remember { mutableStateOf(habit.emoji) }
    var selectedColor by remember { mutableLongStateOf(habit.colorValue) }
    var selectedDays by remember { mutableIntStateOf(habit.targetDays) }
    var selectedCategory by remember { mutableStateOf(habit.category) }

    // Steuerungs-Flag für die Anzeige des Lösch-Bestätigungsdialogs
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Alert-Dialog zur Absicherung des Löschvorgangs
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_habit_title)) },
            text = { Text(stringResource(R.string.delete_habit_message_format, habit.name)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteHabit()
                        onNavigateBack()
                    }
                ) { Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.edit_habit)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    // Lösch-Icon in der TopAppBar
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = stringResource(R.string.cd_delete_habit),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
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

            // Eingabefeld für den Namen
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.habit_name_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Eingabefeld für die Beschreibung
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(stringResource(R.string.description_label)) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2,
                shape = RoundedCornerShape(12.dp)
            )

            // Emoji-Auswahl (horizontal scrollbar)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.section_icon), style = MaterialTheme.typography.labelLarge)
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

            // Farb-Auswahl (horizontal scrollbar)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.section_color), style = MaterialTheme.typography.labelLarge)
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

            // Kategorie-Auswahl via FilterChips
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.section_category), style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EDIT_CATEGORIES.forEach { (value, labelRes) ->
                        FilterChip(
                            selected = selectedCategory == value,
                            onClick = { selectedCategory = value },
                            label = { Text(stringResource(labelRes)) }
                        )
                    }
                }
            }

            // Wochentags-Auswahl via FilterChips (Bitmaske)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.repeat_on), style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    stringArrayResource(R.array.weekday_repeat).forEachIndexed { index, label ->
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

            // Dynamische Vorschaukarte für die vorgenommenen Änderungen
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

            // Schaltfläche zum Speichern der Änderungen
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSaveHabit(name, description, selectedEmoji, selectedColor, selectedDays, selectedCategory)
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = name.isNotBlank(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Check, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.save_changes), style = MaterialTheme.typography.titleMedium)
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}