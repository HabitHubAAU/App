package com.example.habithub.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
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
import com.example.habithub.ui.viewmodel.PomodoroPhase
import com.example.habithub.ui.viewmodel.PomodoroViewModel

/**
 * Eine zustandsbehaftete UI-Komponente zur Darstellung und Steuerung des Pomodoro-Timers.
 * Diese Ansicht konsumiert die benötigten Zustände (StateFlows) aus dem [PomodoroViewModel],
 * darunter die verbleibende Zeit, die aktuelle Phase (Arbeitszeit oder Pause) sowie die konfigurierten
 * Intervalldauern.
 *
 * Sie bietet visuelles Feedback über einen kreisförmigen Fortschrittsindikator und stellt
 * Bedienelemente zum Starten, Pausieren, Zurücksetzen sowie zur Anpassung der Phasenlängen bereit.
 *
 * @param viewModel Das ViewModel zur Verwaltung der zugrundeliegenden Timer-Logik und der Phasenübergänge.
 * @param onNavigateBack Ein Callback zur Navigation zurück zum vorherigen Bildschirm.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroScreen(
    viewModel: PomodoroViewModel,
    onNavigateBack: () -> Unit
) {
    // Sammeln der reaktiven Zustände aus dem ViewModel
    val remainingSeconds by viewModel.remainingSeconds.collectAsState()
    val isRunning by viewModel.isRunning.collectAsState()
    val phase by viewModel.phase.collectAsState()
    val workMinutes by viewModel.workMinutes.collectAsState()
    val breakMinutes by viewModel.breakMinutes.collectAsState()
    val completedRounds by viewModel.completedRounds.collectAsState()

    // Gesamtdauer der aktuellen Phase in Sekunden (für den Fortschritts-Kreis)
    val totalSeconds = if (phase == PomodoroPhase.WORK) workMinutes * 60 else breakMinutes * 60

    // Berechnung des relativen Fortschritts (Wert zwischen 0.0 und 1.0)
    val progress = if (totalSeconds > 0) remainingSeconds.toFloat() / totalSeconds.toFloat() else 0f

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.pomodoro_title)) },
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // Visuelle Hervorhebung der aktuellen Phase durch Farbgebung
            val phaseColor = if (phase == PomodoroPhase.WORK)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.tertiary

            Text(
                text = stringResource(phase.labelRes),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = phaseColor
            )

            // Anzeige der bisher abgeschlossenen Lernrunden
            Text(
                text = stringResource(R.string.round_format, completedRounds + 1, completedRounds),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Großer Timer mit animiertem Fortschritts-Kreis
            Box(
                modifier = Modifier.size(240.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 8.dp,
                    color = phaseColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Text(
                    text = formatTime(remainingSeconds),
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Steuerungselemente: Start/Pause und Reset
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = { if (isRunning) viewModel.pause() else viewModel.start() },
                    modifier = Modifier.height(52.dp)
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (isRunning) stringResource(R.string.pause) else stringResource(R.string.start))
                }
                OutlinedButton(
                    onClick = { viewModel.reset() },
                    modifier = Modifier.height(52.dp)
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.reset))
                }
            }

            Spacer(Modifier.height(8.dp))

            // Karte zur Konfiguration der Intervalldauern
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(stringResource(R.string.set_durations), style = MaterialTheme.typography.titleSmall)

                    DurationRow(
                        label = stringResource(R.string.phase_work),
                        minutes = workMinutes,
                        enabled = !isRunning,
                        onMinus = { viewModel.decreaseWork() },
                        onPlus = { viewModel.increaseWork() }
                    )
                    DurationRow(
                        label = stringResource(R.string.phase_break),
                        minutes = breakMinutes,
                        enabled = !isRunning,
                        onMinus = { viewModel.decreaseBreak() },
                        onPlus = { viewModel.increaseBreak() }
                    )
                }
            }
        }
    }
}

/**
 * Eine wiederverwendbare UI-Zeile zur Einstellung der Dauer einer spezifischen Pomodoro-Phase.
 * Beinhaltet ein beschreibendes Textlabel sowie Plus- und Minus-Schaltflächen zur Anpassung des Wertes.
 *
 * @param label Die Bezeichnung der einzustellenden Phase (z. B. "Arbeitsphase" oder "Pause").
 * @param minutes Der aktuell eingestellte Wert in Minuten.
 * @param enabled Steuert die Interagierbarkeit der Schaltflächen. Um Inkonsistenzen zu vermeiden,
 *                sollten diese deaktiviert sein, während der Timer aktiv läuft.
 * @param onMinus Callback zur Verringerung der Minutenzahl.
 * @param onPlus Callback zur Erhöhung der Minutenzahl.
 */
@Composable
private fun DurationRow(
    label: String,
    minutes: Int,
    enabled: Boolean,
    onMinus: () -> Unit,
    onPlus: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onMinus, enabled = enabled) {
                Icon(Icons.Filled.Remove, contentDescription = stringResource(R.string.cd_decrease))
            }
            Text(
                text = stringResource(R.string.minutes_format, minutes),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.widthIn(min = 64.dp),
                color = MaterialTheme.colorScheme.onSurface
            )
            IconButton(onClick = onPlus, enabled = enabled) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.cd_increase))
            }
        }
    }
}

/**
 * Hilfsfunktion zur Formatierung einer absoluten Sekundenanzahl in einen
 * standardisierten Zeit-String im Format "MM:SS" (z. B. 05:30).
 *
 * @param totalSeconds Die umzuwandelnde Gesamtzeit in Sekunden.
 * @return Der formatierte Zeit-String mit zweistelligen Minuten und Sekunden.
 */
private fun formatTime(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}