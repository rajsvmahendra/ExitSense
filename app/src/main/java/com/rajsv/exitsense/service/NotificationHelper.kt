package com.rajsv.exitsense.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.rajsv.exitsense.MainActivity
import com.rajsv.exitsense.R
import com.rajsv.exitsense.data.model.SettingsDataStore
import kotlinx.coroutines.flow.first

object NotificationHelper {

    private const val CHANNEL_ID = "exitsense_reminders"
    private const val CHANNEL_NAME = "ExitSense Reminders"
    private const val CHANNEL_DESCRIPTION = "Notifications for item reminders"

    private const val WELCOME_CHANNEL_ID = "exitsense_welcome"
    private const val WELCOME_CHANNEL_NAME = "Welcome Messages"

    // Create notification channels (call this in MainActivity or Application class)
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Reminder channel
            val reminderChannel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                enableLights(true)
            }
            notificationManager.createNotificationChannel(reminderChannel)

            // Welcome channel
            val welcomeChannel = NotificationChannel(
                WELCOME_CHANNEL_ID,
                WELCOME_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Welcome and info messages"
            }
            notificationManager.createNotificationChannel(welcomeChannel)
        }
    }

    // Show welcome notification
    @SuppressLint("MissingPermission")
    suspend fun showWelcomeNotification(context: Context, userName: String) {
        if (!isNotificationToggleEnabled(context)) return
        if (!hasNotificationPermission(context)) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, WELCOME_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Welcome to ExitSense! 🎉")
            .setContentText("Hey $userName! You're all set. Add items and locations to never forget anything again.")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Hey $userName! You're all set. Add items and locations to get smart reminders when you leave. Never forget your essentials again! 🚀"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(1001, notification)
    }

    // Show reminder notification
    @SuppressLint("MissingPermission")
    suspend fun showReminderNotification(
        context: Context,
        notificationId: Int,
        title: String,
        message: String,
        items: List<String> = emptyList(),
        showToastIfDisabled: Boolean = false
    ) {
        if (!isNotificationToggleEnabled(context)) {
            if (showToastIfDisabled) {
                Toast.makeText(context, "Enable notifications in Settings", Toast.LENGTH_SHORT).show()
            }
            return
        }
        if (!hasNotificationPermission(context)) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 250, 250, 250))

        // If there are multiple items, show inbox style
        if (items.isNotEmpty()) {
            val inboxStyle = NotificationCompat.InboxStyle()
                .setBigContentTitle(title)

            items.forEach { item ->
                inboxStyle.addLine("• $item")
            }

            notificationBuilder.setStyle(inboxStyle)
        } else {
            notificationBuilder.setStyle(NotificationCompat.BigTextStyle().bigText(message))
        }

        NotificationManagerCompat.from(context).notify(notificationId, notificationBuilder.build())
    }

    // Show location-based reminder
    suspend fun showLocationReminderNotification(
        context: Context,
        locationName: String,
        items: List<String>
    ) {
        val title = "Don't forget! 📱"
        val message = if (items.size == 1) {
            "You're leaving $locationName. Remember: ${items.first()}"
        } else {
            "You're leaving $locationName. Remember ${items.size} items"
        }

        showReminderNotification(
            context = context,
            notificationId = locationName.hashCode(),
            title = title,
            message = message,
            items = items
        )
    }

    suspend fun isNotificationToggleEnabled(context: Context): Boolean {
        return SettingsDataStore.getNotifications(context).first()
    }

    // Check notification permission
    fun hasNotificationPermission(context: Context): Boolean {
        val hasRuntimePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
        return hasRuntimePermission && NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    // Cancel specific notification
    fun cancelNotification(context: Context, notificationId: Int) {
        NotificationManagerCompat.from(context).cancel(notificationId)
    }

    // Cancel all notifications
    fun cancelAllNotifications(context: Context) {
        NotificationManagerCompat.from(context).cancelAll()
    }
}