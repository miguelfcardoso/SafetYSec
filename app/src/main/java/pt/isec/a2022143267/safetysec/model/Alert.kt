package pt.isec.a2022143267.safetysec.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint

/**
 * Alert data class
 * @param id Alert unique identifier
 * @param protectedId Protected user ID who triggered the alert
 * @param monitorId Monitor user ID to be notified
 * @param ruleId Rule ID that triggered the alert
 * @param alertType Type of alert
 * @param status Alert status
 * @param location Geographic location of the alert
 * @param timestamp When the alert was triggered
 * @param videoUrl URL to the recorded video in Firebase Storage
 * @param cancelledAt When the alert was cancelled (if applicable)
 * @param cancelledBy Who cancelled the alert
 */
data class Alert(
    val id: String = "",
    val protectedId: String = "",
    val monitorId: String = "",
    val ruleId: String = "",
    val alertType: RuleType = RuleType.FALL_DETECTION,
    val status: AlertStatus = AlertStatus.PENDING,
    val location: GeoPoint? = null,
    val timestamp: Timestamp = Timestamp.now(),
    val videoUrl: String = "",
    val cancelledAt: Timestamp? = null,
    val cancelledBy: String = "",
    val additionalData: Map<String, String> = emptyMap() // Para dados extra como velocidade
)

enum class AlertStatus {
    PENDING,      // Alert triggered, waiting for user action
    ACTIVE,       // Alert confirmed, monitors notified
    CANCELLED,    // Alert cancelled by protected user
    RESOLVED      // Alert handled by monitor
}

