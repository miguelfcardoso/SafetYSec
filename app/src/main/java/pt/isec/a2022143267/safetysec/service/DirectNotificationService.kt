package pt.isec.a2022143267.safetysec.service

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import pt.isec.a2022143267.safetysec.model.Alert
import pt.isec.a2022143267.safetysec.model.AlertStatus

/**
 * Alternative notification service that works without Cloud Functions
 * Uses direct FCM token retrieval and Firestore listeners
 */
class DirectNotificationService {
    private val firestore = FirebaseFirestore.getInstance()
    private val messaging = FirebaseMessaging.getInstance()

    /**
     * Send alert notification to all monitors
     * This is called after countdown finishes and alert becomes ACTIVE
     */
    suspend fun sendAlertToMonitors(
        alert: Alert,
        protectedUserName: String
    ): Result<Unit> {
        return try {
            // Get all monitors for this protected user
            val relationsSnapshot = firestore.collection("relations")
                .whereEqualTo("protectedId", alert.protectedId)
                .whereEqualTo("status", "APPROVED")
                .get()
                .await()

            // For each monitor, create a notification document
            relationsSnapshot.documents.forEach { doc ->
                val monitorId = doc.getString("monitorId") ?: return@forEach

                // Create notification document that monitor's app will listen to
                val notificationData = hashMapOf(
                    "type" to "alert",
                    "monitorId" to monitorId,
                    "alertId" to alert.id,
                    "protectedId" to alert.protectedId,
                    "protectedName" to protectedUserName,
                    "alertType" to alert.alertType.name,
                    "timestamp" to alert.timestamp,
                    "latitude" to (alert.location?.latitude ?: 0.0),
                    "longitude" to (alert.location?.longitude ?: 0.0),
                    "videoUrl" to alert.videoUrl,
                    "read" to false,
                    "createdAt" to com.google.firebase.Timestamp.now()
                )

                // Store in notifications collection
                firestore.collection("notifications")
                    .add(notificationData)
                    .await()

                Log.d("DirectNotification", "Notification created for monitor: $monitorId")
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("DirectNotification", "Error sending notifications", e)
            Result.failure(e)
        }
    }

    /**
     * Send video available notification
     */
    suspend fun sendVideoAvailableNotification(
        alertId: String,
        monitorId: String,
        protectedUserName: String,
        videoUrl: String
    ): Result<Unit> {
        return try {
            val notificationData = hashMapOf(
                "type" to "video_available",
                "monitorId" to monitorId,
                "alertId" to alertId,
                "protectedName" to protectedUserName,
                "videoUrl" to videoUrl,
                "read" to false,
                "createdAt" to com.google.firebase.Timestamp.now()
            )

            firestore.collection("notifications")
                .add(notificationData)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Listen for new notifications (called by monitors)
     */
    fun listenForNotifications(
        userId: String,
        onNotification: (Map<String, Any>) -> Unit
    ) {
        firestore.collection("notifications")
            .whereEqualTo("monitorId", userId)
            .whereEqualTo("read", false)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("DirectNotification", "Error listening for notifications", error)
                    return@addSnapshotListener
                }

                snapshot?.documentChanges?.forEach { change ->
                    if (change.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                        val data = change.document.data
                        onNotification(data)

                        // Mark as read
                        change.document.reference.update("read", true)
                    }
                }
            }
    }

    /**
     * Update user's FCM token
     */
    suspend fun updateFCMToken(userId: String): Result<Unit> {
        return try {
            val token = messaging.token.await()

            firestore.collection("users")
                .document(userId)
                .update("fcmToken", token)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

