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

@Composable
fun ConfettiEffect(
    modifier: Modifier = Modifier,
    onAnimationEnd: () -> Unit
) {
    val particles = remember {
        List(150) {
            Particle(
                x = Random.nextFloat(),
                y = -Random.nextFloat() * 0.2f,
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

    var isRunning by remember { mutableStateOf(true) }
    var frame by remember { mutableStateOf(0) }

    LaunchedEffect(isRunning) {
        if (isRunning) {
            val endTime = System.currentTimeMillis() + 2500
            while (System.currentTimeMillis() < endTime) {
                withFrameNanos { }
                particles.forEach {
                    it.x += it.speedX
                    it.y += it.speedY
                    it.rotation += it.rotationSpeed
                }
                frame++
            }
            isRunning = false
            onAnimationEnd()
        }
    }

    if (isRunning) {
        Canvas(modifier = modifier.fillMaxSize()) {
            frame.hashCode()
            val canvasWidth = size.width
            val canvasHeight = size.height
            particles.forEach { p ->
                rotate(
                    degrees = p.rotation,
                    pivot = Offset(p.x * canvasWidth + p.size / 2, p.y * canvasHeight + p.size / 2)
                ) {
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