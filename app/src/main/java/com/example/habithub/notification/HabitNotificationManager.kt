package com.example.habithub.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.habithub.MainActivity
import com.example.habithub.R

/**
 * Verantwortlich für die Erstellung und Verwaltung von lokalen Benachrichtigungen der App.
 * Kapselt die Interaktion mit dem Android-System-Benachrichtigungsdienst.
 */
class HabitNotificationManager(private val context: Context) {

    /**
     * Referenz auf den Android-Systemdienst zur Verwaltung von Benachrichtigungen.
     */
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    /**
     * Stellt sicher, dass der erforderliche Benachrichtigungskanal sofort bei der
     * Instanziierung des Managers angelegt wird.
     */
    init {
        createNotificationChannel()
    }

    /**
     * Erstellt einen Benachrichtigungskanal (Notification Channel).
     * Dies ist ab Android 8.0 (API-Level 26 / Oreo) zwingend erforderlich,
     * damit Benachrichtigungen angezeigt werden können.
     * Auf älteren Android-Versionen wird diese Methode ignoriert.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.channel_description)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Baut eine Benachrichtigung zusammen und zeigt sie dem Nutzer an.
     * Ein Klick auf die Benachrichtigung öffnet die [MainActivity].
     *
     * @param title Der Titel der Benachrichtigung.
     * @param message Der inhaltliche Text der Benachrichtigung.
     * @param notificationId Eine optionale ID für die Benachrichtigung (Standard ist 1).
     *                       Wird dieselbe ID wiederverwendet, überschreibt die neue Benachrichtigung die alte.
     */
    fun showNotification(title: String, message: String, notificationId: Int = 1) {
        // Intent vorbereiten, der beim Klick auf die Benachrichtigung ausgeführt wird
        val intent = Intent(context, MainActivity::class.java).apply {
            // Verhindert, dass die Activity mehrfach auf dem Backstack landet
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        // PendingIntent verpackt den eigentlichen Intent für das System
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        // Konstruktion der eigentlichen Benachrichtigung
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true) // Schließt die Benachrichtigung automatisch nach dem Anklicken

        // Benachrichtigung an das System übergeben
        notificationManager.notify(notificationId, builder.build())
    }

    companion object {
        /** Eindeutige Kennung für den Benachrichtigungskanal dieser App. */
        const val CHANNEL_ID = "habit_hub_notifications"
    }
}