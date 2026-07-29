package com.example.habithub.sensor

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

/**
 * Ein Sensor-Listener zur Erfassung von Schritten mithilfe des geräteinternen Schrittzählers.
 * Der Android-Schrittzähler liefert kontinuierlich die Gesamtzahl der Schritte seit dem letzten
 * Geräteneustart. Diese Klasse berechnet die relativen Schritte seit der Registrierung des Listeners.
 *
 * @param onStepsUpdate Eine Callback-Funktion, die aufgerufen wird, wenn neue Schritte registriert wurden.
 *                      Übergibt die Anzahl der seit der Initialisierung getätigten Schritte.
 */
class StepCounterSensor(private val onStepsUpdate: (Int) -> Unit) : SensorEventListener {

    /**
     * Speichert den initialen Schrittzählerstand zum Zeitpunkt der ersten Messung.
     * Ein Wert von -1f zeigt an, dass noch keine Initialisierung stattgefunden hat.
     */
    private var initialSteps = -1f

    /**
     * Wird vom System aufgerufen, sobald der Schrittzähler-Sensor einen neuen Wert liefert.
     * Berechnet die Differenz zwischen dem aktuellen Gesamtwert und dem [initialSteps]-Wert,
     * um die in der aktuellen Sitzung getätigten Schritte zu ermitteln.
     *
     * @param event Das Sensor-Event mit den aktuellen Schrittdaten.
     */
    override fun onSensorChanged(event: SensorEvent) {
        // Ignoriere alle Events, die nicht vom Schrittzähler stammen
        if (event.sensor.type != Sensor.TYPE_STEP_COUNTER) return

        val total = event.values[0]

        // Setze den initialen Wert bei der ersten erfolgreichen Messung
        if (initialSteps < 0f) initialSteps = total

        // Übermittle die berechneten relativen Schritte an den Callback
        onStepsUpdate((total - initialSteps).toInt())
    }

    /**
     * Wird aufgerufen, wenn sich die Genauigkeit des Sensors ändert.
     * Für diesen Schrittzähler nicht relevant, daher bleibt die Implementierung leer.
     */
    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    companion object {
        /**
         * Registriert den [StepCounterSensor] beim [SensorManager] des Systems.
         * Sucht nach dem Standard-Schrittzähler-Sensor und bindet den Listener.
         *
         * @param sensorManager Der Systemdienst zur Verwaltung der Hardware-Sensoren.
         * @param sensor Die Instanz des StepCounterSensors, die registriert werden soll.
         * @return `true`, falls der Sensor auf dem Gerät vorhanden ist und registriert wurde, andernfalls `false`.
         */
        fun register(sensorManager: SensorManager, sensor: StepCounterSensor): Boolean {
            val s = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) ?: return false
            // Nutzt SENSOR_DELAY_NORMAL für einen batteriefreundlichen Aktualisierungszyklus
            sensorManager.registerListener(sensor, s, SensorManager.SENSOR_DELAY_NORMAL)
            return true
        }

        /**
         * Meldet den [StepCounterSensor] vom [SensorManager] ab.
         * Sollte aufgerufen werden, sobald die Schrittzählung nicht mehr benötigt wird
         * (z. B. wenn die App in den Hintergrund wechselt), um Systemressourcen zu schonen.
         *
         * @param sensorManager Der Systemdienst zur Verwaltung der Hardware-Sensoren.
         * @param sensor Die Instanz des StepCounterSensors, die abgemeldet werden soll.
         */
        fun unregister(sensorManager: SensorManager, sensor: StepCounterSensor) {
            sensorManager.unregisterListener(sensor)
        }
    }
}