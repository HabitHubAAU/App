package com.example.habithub.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.habithub.R
import java.util.concurrent.Executors

/**
 * Eine zustandsbehaftete UI-Komponente zur optischen Erfassung der Herzfrequenz (Pulsmessung).
 *
 * Diese Ansicht nutzt die Smartphone-Kamera und die aktivierte LED-Taschenlampe (via CameraX),
 * um durch Photoplethysmographie (PPG) minimale Farb- bzw. Helligkeitsveränderungen der Haut
 * an der aufgelegten Fingerkuppe zu erkennen. Diese Veränderungen korrelieren mit dem
 * pulssynchronen Blutfluss.
 *
 * Der integrierte Algorithmus analysiert kontinuierlich den zentralen Bildausschnitt,
 * wendet einen Tiefpassfilter (exponentielle Glättung) an und berechnet die Schläge pro
 * Minute (BPM) basierend auf den zeitlichen Abständen der erkannten Helligkeits-Gipfel (Peaks).
 *
 * @param onNavigateBack Ein Callback zur Navigation zurück zum vorherigen Bildschirm.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PulseScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Verwaltung der Kamera-Berechtigung
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    // UI- und Messzustände
    var measuring by remember { mutableStateOf(false) }
    var bpm by remember { mutableStateOf<Int?>(null) }
    var peakCount by remember { mutableIntStateOf(0) }

    // Animatable für den Herzschlag-Effekt (skaliert das Icon bei jedem erkannten Puls)
    val heartScale = remember { Animatable(1f) }
    LaunchedEffect(peakCount) {
        if (peakCount > 0) {
            heartScale.snapTo(1.5f)
            heartScale.animateTo(1f, animationSpec = tween(300))
        }
    }

    // Primitive Arrays dienen hier als zustandsbehaftete (mutable) Container,
    // um die Werte thread-sicher zwischen dem Compose-Thread und dem CameraX-Analyzer-Thread zu teilen.
    val smoothed = remember { doubleArrayOf(0.0) }
    val prevSmoothed = remember { doubleArrayOf(0.0) }
    val prevRising = remember { booleanArrayOf(false) }
    val lastPeakTime = remember { longArrayOf(0L) }
    val peakIntervals = remember { mutableListOf<Long>() }

    /**
     * Setzt die internen Analysevariablen zurück.
     * Wird aufgerufen, wenn die Messung gestoppt oder die Komponente verworfen wird.
     */
    fun resetAnalysisState() {
        smoothed[0] = 0.0
        prevSmoothed[0] = 0.0
        prevRising[0] = false
        lastPeakTime[0] = 0L
        peakIntervals.clear()
    }

    // Verwaltet den Lebenszyklus der CameraX-Instanz gekoppelt an den Messvorgang
    DisposableEffect(measuring, hasCameraPermission) {
        if (!measuring || !hasCameraPermission) return@DisposableEffect onDispose {}

        val executor = Executors.newSingleThreadExecutor()
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        var boundProvider: ProcessCameraProvider? = null

        cameraProviderFuture.addListener({
            val provider = cameraProviderFuture.get()
            boundProvider = provider

            // Konfiguration der Bildanalyse, verwirft alte Frames zugunsten der Latenz
            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            analysis.setAnalyzer(executor) { imageProxy ->
                // Extraktion der Luminanz-Ebene (Y-Plane aus YUV_420_888)
                val plane = imageProxy.planes[0]
                val buffer = plane.buffer
                val bytes = ByteArray(buffer.remaining())
                buffer.get(bytes)

                val w = imageProxy.width
                val h = imageProxy.height
                val stride = plane.rowStride

                // Begrenzung der Analyse auf das innere Viertel (Zentrum) des Bildes
                val x0 = w / 4; val x1 = 3 * w / 4
                val y0 = h / 4; val y1 = 3 * h / 4
                var sum = 0L; var count = 0
                var row = y0

                // Iteration über die definierten Pixel zur Berechnung der Durchschnittshelligkeit
                while (row < y1) {
                    var col = x0
                    while (col < x1) {
                        val idx = row * stride + col
                        if (idx < bytes.size) { sum += bytes[idx].toInt() and 0xFF; count++ }
                        col += 4 // Überspringt Pixel zur Leistungsoptimierung
                    }
                    row += 4
                }
                val avg = if (count > 0) sum.toDouble() / count else 0.0

                // Exponentielle Glättung (Tiefpassfilter) zur Reduktion von Bildrauschen
                smoothed[0] = 0.7 * smoothed[0] + 0.3 * avg
                val isRising = smoothed[0] > prevSmoothed[0]
                val now = System.currentTimeMillis()

                // Peak-Erkennung: Ein Peak ist erreicht, wenn der Wert nach einem Anstieg wieder fällt
                if (isRising && !prevRising[0]) {
                    prevRising[0] = true
                } else if (!isRising && prevRising[0]) {
                    prevRising[0] = false
                    val elapsed = now - lastPeakTime[0]

                    // Filterung von unrealistischen Intervallen (BPM < 40 oder > 200)
                    if (lastPeakTime[0] > 0 && elapsed in 300..1500) {
                        peakIntervals.add(elapsed)
                        if (peakIntervals.size > 8) peakIntervals.removeAt(0)

                        // Erst nach 3 validen Intervallen wird ein Durchschnitt errechnet
                        if (peakIntervals.size >= 3) {
                            val avgInterval = peakIntervals.average()
                            val newBpm = (60000 / avgInterval).toInt()
                            if (newBpm in 40..200) {
                                bpm = newBpm
                                peakCount++
                            }
                        }
                    }
                    lastPeakTime[0] = now
                }
                prevSmoothed[0] = smoothed[0]

                imageProxy.close()
            }

            provider.unbindAll()
            val camera = provider.bindToLifecycle(
                lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, analysis
            )
            // Aktiviert die Taschenlampe zur Durchleuchtung der Fingerkuppe
            camera.cameraControl.enableTorch(true)
        }, ContextCompat.getMainExecutor(context))

        onDispose {
            boundProvider?.unbindAll()
            executor.shutdown()
            resetAnalysisState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.pulse_title)) },
                navigationIcon = {
                    IconButton(onClick = {
                        measuring = false
                        onNavigateBack()
                    }) {
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
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            // Anzeige-Bereich für den ermittelten Puls und die visuelle Herzschlag-Animation
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Filled.Favorite,
                    contentDescription = null,
                    tint = Color(0xFFE53935),
                    modifier = Modifier
                        .size(64.dp)
                        .scale(heartScale.value)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    bpm?.toString() ?: "--",
                    fontSize = 96.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    stringResource(R.string.bpm),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                // Ladeindikator während der Initialisierungsphase der Messung
                if (measuring && bpm == null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.pulse_hold_finger),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                }
            }

            // Steuerungs-Bereich: Berechtigung anfragen oder Messung starten/stoppen
            if (!hasCameraPermission) {
                Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.pulse_grant_camera))
                }
            } else {
                Button(
                    onClick = {
                        if (measuring) {
                            measuring = false
                            bpm = null
                        } else {
                            bpm = null
                            measuring = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (measuring) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        if (measuring) stringResource(R.string.stop) else stringResource(R.string.pulse_start),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            // Anleitungskarte mit Schritt-für-Schritt-Anweisungen für den Nutzer
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        stringResource(R.string.pulse_how_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(stringResource(R.string.pulse_step_1), style = MaterialTheme.typography.bodySmall)
                    Text(stringResource(R.string.pulse_step_2), style = MaterialTheme.typography.bodySmall)
                    Text(stringResource(R.string.pulse_step_3), style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        stringResource(R.string.pulse_disclaimer),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}