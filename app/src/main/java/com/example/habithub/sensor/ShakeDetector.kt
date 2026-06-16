package com.example.habithub.sensor

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

class ShakeDetector(private val onShake: () -> Unit) : SensorEventListener {

    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 0f
    private var initialized = false
    private var lastShakeTime = 0L

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        if (!initialized) {
            lastX = x; lastY = y; lastZ = z
            initialized = true
            return
        }

        val delta = sqrt(
            (x - lastX) * (x - lastX) +
                    (y - lastY) * (y - lastY) +
                    (z - lastZ) * (z - lastZ)
        )
        lastX = x; lastY = y; lastZ = z

        if (delta > SHAKE_THRESHOLD) {
            val now = System.currentTimeMillis()
            if (now - lastShakeTime > COOLDOWN_MS) {
                lastShakeTime = now
                onShake()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    companion object {
        private const val SHAKE_THRESHOLD = 12f
        private const val COOLDOWN_MS = 1500L

        fun register(sensorManager: SensorManager, detector: ShakeDetector): Boolean {
            val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) ?: return false
            sensorManager.registerListener(detector, sensor, SensorManager.SENSOR_DELAY_UI)
            return true
        }

        fun unregister(sensorManager: SensorManager, detector: ShakeDetector) {
            sensorManager.unregisterListener(detector)
        }
    }
}
