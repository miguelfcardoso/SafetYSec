package pt.isec.a2022143267.safetysec.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import pt.isec.a2022143267.safetysec.model.Alert
import pt.isec.a2022143267.safetysec.model.AlertStatus

/**
 * Repository for Alert operations with Firestore
 */
class AlertRepository {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    /**
     * Create a new alert
     */
    suspend fun createAlert(alert: Alert): Result<String> {
        return try {
            val docRef = firestore.collection("alerts").document()
            val alertWithId = alert.copy(id = docRef.id)
            docRef.set(alertWithId).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update alert status
     */
    suspend fun updateAlertStatus(alertId: String, status: AlertStatus): Result<Unit> {
        return try {
            firestore.collection("alerts")
                .document(alertId)
                .update("status", status.name)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update alert with video URL
     */
    suspend fun updateAlertVideoUrl(alertId: String, videoUrl: String): Result<Unit> {
        return try {
            firestore.collection("alerts")
                .document(alertId)
                .update("videoUrl", videoUrl)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get alerts for a monitor (real-time)
     */
    fun getAlertsForMonitor(monitorId: String): Flow<List<Alert>> = callbackFlow {
        val listener = firestore.collection("alerts")
            .whereEqualTo("monitorId", monitorId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val alerts = snapshot?.documents?.mapNotNull {
                    it.toObject(Alert::class.java)
                } ?: emptyList()

                trySend(alerts)
            }

        awaitClose { listener.remove() }
    }

    /**
     * Get alerts for a protected user (real-time)
     */
    fun getAlertsForProtected(protectedId: String): Flow<List<Alert>> = callbackFlow {
        val listener = firestore.collection("alerts")
            .whereEqualTo("protectedId", protectedId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val alerts = snapshot?.documents?.mapNotNull {
                    it.toObject(Alert::class.java)
                } ?: emptyList()

                trySend(alerts)
            }

        awaitClose { listener.remove() }
    }

    /**
     * Get active alerts for a monitor
     */
    fun getActiveAlertsForMonitor(monitorId: String): Flow<List<Alert>> = callbackFlow {
        val listener = firestore.collection("alerts")
            .whereEqualTo("monitorId", monitorId)
            .whereEqualTo("status", AlertStatus.ACTIVE.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val alerts = snapshot?.documents?.mapNotNull {
                    it.toObject(Alert::class.java)
                } ?: emptyList()

                trySend(alerts)
            }

        awaitClose { listener.remove() }
    }

    /**
     * Cancel an alert
     */
    suspend fun cancelAlert(alert: Alert, cancelledBy: String): Result<Unit> {
        return try {
            val updatedAlert = alert.copy(
                status = AlertStatus.CANCELLED,
                cancelledBy = cancelledBy,
                cancelledAt = com.google.firebase.Timestamp.now()
            )

            firestore.collection("alerts")
                .document(alert.id)
                .set(updatedAlert)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get all approved monitor-protected relations for a protected user
     */
    suspend fun getMonitorsForProtected(protectedId: String): List<pt.isec.a2022143267.safetysec.model.MonitorProtectedRelation> {
        return try {
            val snapshot = firestore.collection("relations")
                .whereEqualTo("protectedId", protectedId)
                .whereEqualTo("status", pt.isec.a2022143267.safetysec.model.RelationStatus.APPROVED.name)
                .get()
                .await()

            snapshot.documents.mapNotNull {
                it.toObject(pt.isec.a2022143267.safetysec.model.MonitorProtectedRelation::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Delete an alert
     */
    suspend fun deleteAlert(alertId: String): Result<Unit> {
        return try {
            firestore.collection("alerts")
                .document(alertId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get alert by ID
     */
    suspend fun getAlertById(alertId: String): Result<Alert> {
        return try {
            val document = firestore.collection("alerts")
                .document(alertId)
                .get()
                .await()

            val alert = document.toObject(Alert::class.java)
            if (alert != null) {
                Result.success(alert)
            } else {
                Result.failure(Exception("Alert not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get recent alerts for a protected user (non-realtime, for status check)
     */
    suspend fun getRecentAlertsForProtected(protectedId: String, limit: Int = 10): List<Alert> {
        return try {
            val snapshot = firestore.collection("alerts")
                .whereEqualTo("protectedId", protectedId)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            snapshot.documents.mapNotNull {
                it.toObject(Alert::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Get count of active alerts for a protected user
     */
    suspend fun getActiveAlertsCountForProtected(protectedId: String): Int {
        return try {
            val snapshot = firestore.collection("alerts")
                .whereEqualTo("protectedId", protectedId)
                .whereEqualTo("status", AlertStatus.ACTIVE.name)
                .get()
                .await()

            snapshot.documents.size
        } catch (e: Exception) {
            0
        }
    }
}
