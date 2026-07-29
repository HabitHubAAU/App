package com.example.habithub.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.random.Random

/**
 * Repräsentiert ein einzelnes Konfetti-Partikel für die Animationsdarstellung.
 * Die Positions- und Geschwindigkeitswerte (x, y, speedX, speedY) werden als relative
 * Faktoren (z. B. 0.0 bis 1.0) gespeichert und erst beim Zeichnen mit der tatsächlichen
 * Canvas-Größe multipliziert.
 *
 * @property x Die aktuelle horizontale Position (relativ zur Canvas-Breite).
 * @property y Die aktuelle vertikale Position (relativ zur Canvas-Höhe). Startet meist im negativen Bereich, um von oben hereinzufallen.
 * @property speedX Die horizontale Bewegungsgeschwindigkeit pro gerendertem Frame.
 * @property speedY Die vertikale Fallgeschwindigkeit pro gerendertem Frame.
 * @property color Die zugewiesene Farbe des Partikels.
 * @property size Die Kantenlänge des (quadratischen) Partikels in Pixeln.
 * @property rotation Der aktuelle Rotationswinkel in Grad (0 bis 360).
 * @property rotationSpeed Die Rotationsänderung in Grad pro Frame.
 */
data class Particle(
    var x: Float,
    var y: Float,
    var speedX: Float,
    var speedY: Float,
    val color: Color,
    val size: Float,
    var rotation: Float,
    var rotationSpeed: Float
)

/**
 * Eine visuelle Jetpack Compose-Komponente, die einen Konfettiregen über den gesamten Bildschirm zeichnet.
 * Die Animation instanziiert 150 [Particle]-Objekte und berechnet deren Fall- und Rotationsphysik
 * für eine Dauer von 2500 Millisekunden.
 *
 * @param modifier Ein optionaler [Modifier] zur Anpassung des Layouts. Intern wird zwingend [fillMaxSize] angehängt,
 *                 damit der Effekt den gesamten verfügbaren Raum einnimmt.
 * @param onAnimationEnd Eine Callback-Funktion, die exakt einmal aufgerufen wird, sobald die
 *                       vorgegebene Animationsdauer abgelaufen ist und das Konfetti verschwindet.
 */
@Composable
fun ConfettiEffect(
    modifier: Modifier = Modifier,
    onAnimationEnd: () -> Unit
) {
    // Initialisierung von 150 Partikeln mit zufälligen Startwerten, Farben und Geschwindigkeiten
    val particles = remember {
        List(150) {
            Particle(
                x = Random.nextFloat(),
                y = -Random.nextFloat() * 0.2f, // Startet leicht oberhalb des sichtbaren Bereichs
                speedX = (Random.nextFloat() - 0.5f) * 0.015f,
                speedY = Random.nextFloat() * 0.015f + 0.005f,
                color = listOf(
                    Color(0xFFE57373),
                    Color(0xFF81C784),
                    Color(0xFF64B5F6),
                    Color(0xFFFFD54F),
                    Color(0xFFBA68C8),
                    Color(0xFF4DD0E1)
                ).random(),
                size = Random.nextFloat() * 25f + 15f,
                rotation = Random.nextFloat() * 360f,
                rotationSpeed = (Random.nextFloat() - 0.5f) * 15f
            )
        }
    }

    // Steuert den Lebenszyklus der Animation
    var isRunning by remember { mutableStateOf(true) }
    // Triggert den Recompose-Zyklus des Canvas in jedem Frame
    var frame by remember { mutableStateOf(0) }

    // Animations-Schleife, die an den 'isRunning'-Status gebunden ist
    LaunchedEffect(isRunning) {
        if (isRunning) {
            val endTime = System.currentTimeMillis() + 2500

            // Führt die Berechnungen so lange durch, bis das Zeitlimit von 2,5 Sekunden erreicht ist
            while (System.currentTimeMillis() < endTime) {
                // Wartet auf den nächsten verfügbaren Render-Frame, um CPU-Zeit zu sparen und flüssig zu bleiben
                withFrameNanos { }

                // Aktualisiert die Physik (Position und Rotation) für jedes Partikel
                particles.forEach {
                    it.x += it.speedX
                    it.y += it.speedY
                    it.rotation += it.rotationSpeed
                }

                // Inkrementiert den Frame-Counter, um das Neuzeichnen (Recomposition) des Canvas auszulösen
                frame++
            }

            // Beendet die Animation und ruft den Callback auf
            isRunning = false
            onAnimationEnd()
        }
    }

    // Zeichnet die Partikel auf den Bildschirm, solange die Animation aktiv ist
    if (isRunning) {
        Canvas(modifier = modifier.fillMaxSize()) {
            // Expliziter Aufruf von frame.hashCode(), um dem Compose-Compiler mitzuteilen,
            // dass dieser Block vom 'frame'-State abhängt und bei Änderung neu gezeichnet werden muss.
            frame.hashCode()

            val canvasWidth = size.width
            val canvasHeight = size.height

            particles.forEach { p ->
                // Rotiert den Zeichenbereich um den Mittelpunkt des jeweiligen Partikels
                rotate(
                    degrees = p.rotation,
                    pivot = Offset(p.x * canvasWidth + p.size / 2, p.y * canvasHeight + p.size / 2)
                ) {
                    // Zeichnet das Partikel als Quadrat mit den berechneten absoluten Koordinaten
                    drawRect(
                        color = p.color,
                        topLeft = Offset(p.x * canvasWidth, p.y * canvasHeight),
                        size = Size(p.size, p.size)
                    )
                }
            }
        }
    }
}