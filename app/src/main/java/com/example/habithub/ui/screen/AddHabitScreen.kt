package com.example.habithub.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.habithub.R
import com.example.habithub.ui.theme.HabitHubTheme
import com.example.habithub.ui.viewmodel.HabitViewModel

/**
 * Eine vordefinierte Liste von Emojis zur visuellen Repräsentation einer Gewohnheit.
 */
private val PRESET_EMOJIS = listOf(
    "⭐", "💪", "🏃", "📚", "💧", "🧘", "🎯", "🌙", "☀️",
    "🍎", "✍️", "🎵", "💊", "🧹", "💻", "🌿", "🔥", "❤️", "🎨", "🏋️"
)

/**
 * Eine vordefinierte Liste von ARGB-Farbwerten (als Long) zur Einfärbung der UI-Elemente einer Gewohnheit.
 */
private val PRESET_COLORS = listOf(
    0xFF6750A4L, 0xFF00897BL, 0xFFE53935L, 0xFF43A047L,
    0xFF1E88E5L, 0xFFFB8C00L, 0xFFD81B60L, 0xFF546E7AL
)

/**
 * Eine vordefinierte Liste von verfügbaren Kategorien für Gewohnheiten,
 * bestehend aus einem internen Schlüssel und der zugehörigen String-Ressourcen-ID.
 */
private val CATEGORIES = listOf(
    "hobby" to R.string.category_hobby,
    "study" to R.string.category_study,
    "work" to R.string.category_work
)

/**
 * Ein Wrapper-Composable für den Bildschirm zum Erstellen einer neuen Gewohnheit.
 * Diese Komponente bindet das [HabitViewModel] an die eigentliche UI-Darstellung
 * ([AddHabitScreenContent]) und delegiert das finale Speichern der eingegebenen Daten an das ViewModel.
 *
 * @param viewModel Das ViewModel zur Verwaltung der Gewohnheitsdaten in der Datenbank.
 * @param onNavigateBack Ein Callback zur Rückkehr zum vorherigen Bildschirm.
 */
@Composable
fun AddHabitScreen(
    viewModel: HabitViewModel,
    onNavigateBack: () -> Unit
) {
    AddHabitScreenContent(
        onAddHabit = { name, description, emoji, color, days, category ->
            viewModel.addHabit(name, description, emoji, color, days, category)
        },
        onNavigateBack = onNavigateBack
    )
}

/**
 * Die zustandsbehaftete (stateful) UI-Kernkomponente für das Formular zur Anlage einer neuen Gewohnheit.
 * Verwaltet den internen Status der Eingabefelder und stellt interaktive Auswahlmöglichkeiten
 * für Name, Beschreibung, Symbol, Farbe, Kategorie und Wiederholungsrhythmus bereit.
 *
 * @param onAddHabit Ein Callback, der beim Klick auf die Speichern-Schaltfläche ausgelöst wird
 *                   und die validierten Formulardaten übergibt. Die Wochentage werden als Integer-Bitmaske übergeben.
 * @param onNavigateBack Ein Callback zur Navigation, ausgelöst durch den Zurück-Pfeil oder nach erfolgreichem Speichern.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitScreenContent(
    onAddHabit: (String, String, String, Long, Int, String) -> Unit,
    onNavigateBack: () -> Unit
) {
    // Lokale Zustände für die Eingabefelder des Formulars
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedEmoji by remember { mutableStateOf("⭐") }
    var selectedColor by remember { mutableLongStateOf(0xFF6750A4L) }

    // Die ausgewählten Wochentage werden als Bitmaske gespeichert (Standard: 0b1111111 für alle 7 Tage)
    var selectedDays by remember { mutableIntStateOf(0b1111111) }
    var selectedCategory by remember { mutableStateOf("hobby") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.new_habit)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
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

            // Eingabefeld für den Namen der Gewohnheit (Pflichtfeld)
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.habit_name_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Eingabefeld für eine optionale Beschreibung
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(stringResource(R.string.description_label)) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2,
                shape = RoundedCornerShape(12.dp)
            )

            // Horizontal scrollbare Liste zur Auswahl eines repräsentativen Emojis
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.section_icon), style = MaterialTheme.typography.labelLarge)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(PRESET_EMOJIS) { emoji ->
                        val selected = emoji == selectedEmoji
                        Surface(
                            onClick = { selectedEmoji = emoji },
                            shape = CircleShape,
                            color = if (selected)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(52.dp),
                            border = if (selected)
                                BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                            else null
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(emoji, fontSize = 26.sp)
                            }
                        }
                    }
                }
            }

            // Horizontal scrollbare Liste zur Auswahl der Design-Farbe der Gewohnheit
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.section_color), style = MaterialTheme.typography.labelLarge)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(PRESET_COLORS) { color ->
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
                            // Zeichnet ein Häkchen-Icon über der aktuell ausgewählten Farbe
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

            // Auswahlchips für die Zuweisung einer inhaltlichen Kategorie
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.section_category), style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CATEGORIES.forEach { (value, labelRes) ->
                        FilterChip(
                            selected = selectedCategory == value,
                            onClick = { selectedCategory = value },
                            label = { Text(stringResource(labelRes)) }
                        )
                    }
                }
            }

            // Auswahlchips für die Wochentage, an denen die Gewohnheit fällig ist.
            // Nutzt bitweise Operationen zum Setzen oder Löschen des spezifischen Tages-Bits.
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.repeat_on), style = MaterialTheme.typography.labelLarge)
                val dayLabels = stringArrayResource(R.array.weekday_repeat).toList()
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    itemsIndexed(dayLabels) { index, label ->
                        val isSelected = (selectedDays and (1 shl index)) != 0
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedDays = if (isSelected)
                                    selectedDays and (1 shl index).inv() // Löscht das Bit
                                else
                                    selectedDays or (1 shl index)        // Setzt das Bit
                            },
                            label = { Text(label, fontSize = 12.sp) }
                        )
                    }
                }
            }

            // Dynamische Vorschaukarte der Gewohnheit (wird nur angezeigt, wenn ein Name eingegeben wurde)
            if (name.isNotBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(Color(selectedColor)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(selectedEmoji, fontSize = 26.sp)
                        }
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

            // Schaltfläche zum Speichern (nur klickbar, wenn der Name nicht leer ist)
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onAddHabit(name, description, selectedEmoji, selectedColor, selectedDays, selectedCategory)
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = name.isNotBlank(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Check, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.save_habit), style = MaterialTheme.typography.titleMedium)
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

/**
 * Standard-Vorschau für den [AddHabitScreenContent] in der Android Studio Design-Ansicht.
 */
@Preview(showBackground = true)
@Composable
fun AddHabitScreenPreview() {
    HabitHubTheme {
        AddHabitScreenContent(
            onAddHabit = { _, _, _, _, _, _ -> },
            onNavigateBack = {}
        )
    }
}