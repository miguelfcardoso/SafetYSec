package pt.isec.a2022143267.safetysec.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import pt.isec.a2022143267.safetysec.MainActivity
import pt.isec.a2022143267.safetysec.R

/**
 * Foreground service that listens for notifications in real-time
 * This works without Cloud Functions by listening to Firestore documents
 */
class NotificationListenerService : Service() {

    private val auth = FirebaseAuth.getInstance()
    private val notificationService = DirectNotificationService()
    private val notificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    companion object {
        private const val NOTIFICATION_ID = 2
        private const val CHANNEL_ID = "notification_listener_channel"
        private const val ALERT_CHANNEL_ID = "alerts_channel"

        fun start(context: Context) {
            val intent = Intent(context, NotificationListenerService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, NotificationListenerService::class.java)
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        startForeground(NOTIFICATION_ID, createServiceNotification())
        startListeningForNotifications()
    }

    private fun startListeningForNotifications() {
        val userId = auth.currentUser?.uid ?: return

        notificationService.listenForNotifications(userId) { notificationData ->
            when (notificationData["type"] as? String) {
                "alert" -> handleAlertNotification(notificationData)
                "video_available" -> handleVideoNotification(notificationData)
            }
        }
    }

    private fun handleAlertNotification(data: Map<String, Any>) {
        val protectedName = data["protectedName"] as? String ?: "Unknown"
        val alertType = data["alertType"] as? String ?: "Alert"
        val alertId = data["alertId"] as? String ?: ""

        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("alertId", alertId)
            putExtra("navigateTo", "alert_details")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            alertId.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, ALERT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("⚠️ Safety Alert from $protectedName")
            .setContentText("Alert Type: ${alertType.replace("_", " ")}")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 500, 250, 500))
            .build()

        notificationManager.notify(alertId.hashCode(), notification)
    }

    private fun handleVideoNotification(data: Map<String, Any>) {
        val protectedName = data["protectedName"] as? String ?: "Unknown"
        val alertId = data["alertId"] as? String ?: ""
        val videoUrl = data["videoUrl"] as? String ?: ""

        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("alertId", alertId)
            putExtra("videoUrl", videoUrl)
            putExtra("navigateTo", "alert_details")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            (alertId + "_video").hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, ALERT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("📹 Video Available")
            .setContentText("Video from $protectedName's alert is now available")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify((alertId + "_video").hashCode(), notification)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Service notification channel
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Notification Listener",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Listening for safety alerts"
            }

            // Alert notification channel
            val alertChannel = NotificationChannel(
                ALERT_CHANNEL_ID,
                "Safety Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Safety alerts from protected users"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 250, 500)
            }

            notificationManager.createNotificationChannel(serviceChannel)
            notificationManager.createNotificationChannel(alertChannel)
        }
    }

    private fun createServiceNotification() = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle(getString(R.string.app_name))
        .setContentText("Listening for alerts")
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentIntent(
            PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE
            )
        )
        .build()

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

