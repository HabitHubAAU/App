package com.example.habithub.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.habithub.R
import com.example.habithub.ui.theme.HabitHubTheme

/**
 * Eine zustandslose (stateless) UI-Komponente zur Verwaltung der globalen App-Einstellungen.
 * Dieser Bildschirm ermöglicht die Konfiguration von UI-Darstellung (Dark Mode), Lokalisierung (Sprache)
 * und Benachrichtigungsfreigaben.
 *
 * Die Komponente delegiert sämtliche Zustandsänderungen über Callback-Funktionen nach oben
 * (State Hoisting), typischerweise an ein DataStore-gestütztes ViewModel.
 *
 * @param isDarkTheme Gibt an, ob das dunkle Design aktuell aktiv ist.
 * @param onToggleTheme Callback-Funktion zum Umschalten zwischen hellem und dunklem Design.
 * @param notificationsEnabled Gibt an, ob lokale Benachrichtigungen für Gewohnheiten aktiviert sind.
 * @param onToggleNotifications Callback-Funktion zum Aktivieren oder Deaktivieren der Benachrichtigungen.
 * @param currentLanguage Der Sprachcode der aktuell ausgewählten App-Sprache (z. B. "de" oder "en").
 * @param onSelectLanguage Callback-Funktion zur Änderung der Lokalisierung zur Laufzeit.
 * @param onNavigateBack Callback-Funktion zur Navigation zurück zum vorherigen Bildschirm.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    notificationsEnabled: Boolean,
    onToggleNotifications: (Boolean) -> Unit,
    currentLanguage: String,
    onSelectLanguage: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    // Dynamische Ermittlung der aktuellen App-Version aus dem PackageManager.
    // Ein Fallback auf "1.0" wird verwendet, falls die PackageInfo nicht gelesen werden kann.
    val versionName = remember {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull() ?: "1.0"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Sektion: Darstellung (Theme-Konfiguration)
            SettingsSection(title = stringResource(R.string.section_appearance)) {
                SettingsToggleRow(
                    icon = Icons.Filled.DarkMode,
                    title = stringResource(R.string.dark_mode),
                    subtitle = if (isDarkTheme) stringResource(R.string.dark_mode_on)
                    else stringResource(R.string.dark_mode_off),
                    checked = isDarkTheme,
                    onCheckedChange = { onToggleTheme() }
                )
            }

            // Sektion: Lokalisierung (Sprachauswahl)
            SettingsSection(title = stringResource(R.string.section_language)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = currentLanguage == "de",
                        onClick = { onSelectLanguage("de") },
                        label = { Text("Deutsch") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = currentLanguage != "de",
                        onClick = { onSelectLanguage("en") },
                        label = { Text("English") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Sektion: Benachrichtigungen
            SettingsSection(title = stringResource(R.string.section_notifications)) {
                SettingsToggleRow(
                    icon = Icons.Filled.Notifications,
                    title = stringResource(R.string.notifications),
                    subtitle = stringResource(R.string.notifications_subtitle),
                    checked = notificationsEnabled,
                    onCheckedChange = onToggleNotifications
                )
            }

            // Sektion: App-Metadaten
            SettingsSection(title = stringResource(R.string.section_about)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        stringResource(R.string.version_format, versionName),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.about_tagline),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Eine wiederverwendbare Layout-Komponente für Einstellungssektionen.
 * Gruppiert inhaltliche Einstellungen in einer visuell abgehobenen Card unter einem gemeinsamen Titel.
 *
 * @param title Der Bezeichner bzw. Überschrift der Sektion.
 * @param content Der Composable-Inhalt (Slot), der innerhalb der Sektionskarte gerendert wird.
 */
@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 4.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            content()
        }
    }
}

/**
 * Eine wiederverwendbare UI-Zeile für boolesche Einstellungen.
 * Besteht aus einem beschreibenden Icon, einem Titel, einem Untertitel und einem Schalter (Switch).
 *
 * @param icon Das Vector-Icon zur visuellen Repräsentation der Einstellung.
 * @param title Der Haupttitel der Einstellung.
 * @param subtitle Ein ergänzender Beschreibungstext oder der aktuelle Status.
 * @param checked Der aktuelle Wahrheitswert der Einstellung.
 * @param onCheckedChange Callback, der bei Interaktion mit dem Switch den neuen Zustand übergibt.
 */
@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.width(12.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

/**
 * Standard-Vorschau für den Einstellungsbildschirm im Android Studio Preview-Werkzeug.
 */
@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    HabitHubTheme {
        SettingsScreen(
            isDarkTheme = false,
            onToggleTheme = {},
            notificationsEnabled = true,
            onToggleNotifications = {},
            currentLanguage = "de",
            onSelectLanguage = {},
            onNavigateBack = {}
        )
    }
}