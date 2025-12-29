package pt.isec.a2022143267.safetysec.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pt.isec.a2022143267.safetysec.MainActivity
import pt.isec.a2022143267.safetysec.R

/**
 * Service to handle Firebase Cloud Messaging notifications
 */
class FirebaseMessagingService : FirebaseMessagingService() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    companion object {
        private const val CHANNEL_ID = "alerts_channel"
        private const val CHANNEL_NAME = "Safety Alerts"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Handle incoming notifications
        remoteMessage.notification?.let { notification ->
            sendNotification(
                notification.title ?: "SafetYSec Alert",
                notification.body ?: "New alert received"
            )
        }

        // Handle data payload
        remoteMessage.data.isNotEmpty().let {
            handleDataPayload(remoteMessage.data)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Save token to Firestore for the current user
        val userId = auth.currentUser?.uid
        if (userId != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    firestore.collection("users")
                        .document(userId)
                        .update("fcmToken", token)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun handleDataPayload(data: Map<String, String>) {
        // Process the data payload
        val alertType = data["alertType"]
        val alertId = data["alertId"]
        val userId = data["userId"]

        // You can trigger specific actions based on the data
        // For example, open a specific screen or update local data
    }

    private fun sendNotification(title: String, messageBody: String) {
        createNotificationChannel()

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for safety alerts"
                enableVibration(true)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}

