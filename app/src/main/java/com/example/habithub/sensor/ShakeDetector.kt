package com.example.habithub.sensor

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

/**
 * Ein Sensor-Listener zur Erkennung von physischen Schüttelbewegungen des Geräts.
 * Nutzt den Beschleunigungssensor (Accelerometer), um abrupte Bewegungsänderungen zu registrieren.
 *
 * @param onShake Eine Callback-Funktion, die ausgeführt wird, sobald eine gültige Schüttelbewegung erkannt wurde.
 */
class ShakeDetector(private val onShake: () -> Unit) : SensorEventListener {

    // Speichern der Beschleunigungswerte der jeweils vorherigen Messung
    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 0f

    // Flag, um die Initialisierung bei der ersten Messung zu überprüfen
    private var initialized = false

    // Zeitstempel der letzten erkannten Schüttelbewegung für den Cooldown-Mechanismus
    private var lastShakeTime = 0L

    /**
     * Wird vom System aufgerufen, sobald der Sensor neue Daten liefert.
     * Berechnet die euklidische Distanz der Beschleunigungsvektoren zwischen der aktuellen
     * und der letzten Messung. Überschreitet diese Differenz den [SHAKE_THRESHOLD] und
     * ist die Abklingzeit ([COOLDOWN_MS]) abgelaufen, wird [onShake] ausgelöst.
     *
     * @param event Das Sensor-Event, das die aktuellen Beschleunigungsdaten enthält.
     */
    override fun onSensorChanged(event: SensorEvent) {
        // Ignoriere alle Events, die nicht vom Beschleunigungssensor stammen
        if (event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        // Bei der allerersten Messung nur die Startwerte setzen
        if (!initialized) {
            lastX = x; lastY = y; lastZ = z
            initialized = true
            return
        }

        // Berechnung der Beschleunigungsdifferenz zur vorherigen Messung
        val delta = sqrt(
            (x - lastX) * (x - lastX) +
                    (y - lastY) * (y - lastY) +
                    (z - lastZ) * (z - lastZ)
        )

        // Aktuelle Werte für die nächste Messung speichern
        lastX = x; lastY = y; lastZ = z

        // Prüfen, ob die Bewegung stark genug war
        if (delta > SHAKE_THRESHOLD) {
            val now = System.currentTimeMillis()
            // Prüfen, ob die Abklingzeit seit dem letzten Schütteln vergangen ist
            if (now - lastShakeTime > COOLDOWN_MS) {
                lastShakeTime = now
                onShake()
            }
        }
    }

    /**
     * Wird aufgerufen, wenn sich die Genauigkeit des Sensors ändert.
     * Für die Schüttelerkennung nicht relevant, die Implementierung bleibt daher leer.
     */
    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    companion object {
        /**
         * Schwellenwert für die Beschleunigungsänderung.
         * Je höher der Wert, desto kräftiger muss geschüttelt werden.
         */
        private const val SHAKE_THRESHOLD = 12f

        /**
         * Abklingzeit in Millisekunden zwischen zwei erkannten Schüttel-Events.
         * Verhindert, dass eine einzige, längere Bewegung mehrfache Callbacks auslöst.
         */
        private const val COOLDOWN_MS = 1500L

        /**
         * Registriert den [ShakeDetector] beim [SensorManager] des Systems.
         * Sucht nach dem Standard-Beschleunigungssensor und bindet den Listener.
         *
         * @param sensorManager Der Systemdienst zur Verwaltung der Hardware-Sensoren.
         * @param detector Die Instanz des ShakeDetectors, die registriert werden soll.
         * @return `true`, falls der Sensor vorhanden ist und registriert wurde, andernfalls `false`.
         */
        fun register(sensorManager: SensorManager, detector: ShakeDetector): Boolean {
            val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) ?: return false
            sensorManager.registerListener(detector, sensor, SensorManager.SENSOR_DELAY_UI)
            return true
        }

        /**
         * Meldet den [ShakeDetector] vom [SensorManager] ab.
         * Sollte unbedingt aufgerufen werden (z. B. in onPause oder onDestroy),
         * um den Akkuverbrauch zu reduzieren, wenn die App im Hintergrund ist.
         *
         * @param sensorManager Der Systemdienst zur Verwaltung der Hardware-Sensoren.
         * @param detector Die Instanz des ShakeDetectors, die abgemeldet werden soll.
         */
        fun unregister(sensorManager: SensorManager, detector: ShakeDetector) {
            sensorManager.unregisterListener(detector)
        }
    }
}