package com.example.habithub.sensor

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class StepCounterSensor(private val onStepsUpdate: (Int) -> Unit) : SensorEventListener {

    private var initialSteps = -1f

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_STEP_COUNTER) return
        val total = event.values[0]
        if (initialSteps < 0f) initialSteps = total
        onStepsUpdate((total - initialSteps).toInt())
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    companion object {
        fun register(sensorManager: SensorManager, sensor: StepCounterSensor): Boolean {
            val s = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) ?: return false
            sensorManager.registerListener(sensor, s, SensorManager.SENSOR_DELAY_NORMAL)
            return true
        }

        fun unregister(sensorManager: SensorManager, sensor: StepCounterSensor) {
            sensorManager.unregisterListener(sensor)
        }
    }
}
