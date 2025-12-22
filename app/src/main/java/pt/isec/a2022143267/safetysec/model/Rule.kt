package pt.isec.a2022143267.safetysec.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint

/**
 * Rule data class
 * @param id Rule unique identifier
 * @param monitorId Monitor user ID who created the rule
 * @param protectedId Protected user ID
 * @param ruleType Type of rule (FALL, GEOFENCING, SPEED, INACTIVITY)
 * @param name Rule name
 * @param description Rule description
 * @param isEnabled Whether the rule is active
 * @param parameters Rule-specific parameters
 * @param createdAt Rule creation timestamp
 * @param updatedAt Last update timestamp
 */
data class Rule(
    val id: String = "",
    val monitorId: String = "",
    val protectedId: String = "",
    val ruleType: RuleType = RuleType.FALL_DETECTION,
    val name: String = "",
    val description: String = "",
    val isEnabled: Boolean = true,
    val parameters: RuleParameters = RuleParameters(),
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)

enum class RuleType {
    FALL_DETECTION,
    ACCIDENT,
    GEOFENCING,
    SPEED_CONTROL,
    INACTIVITY,
    PANIC_BUTTON
}

/**
 * Rule parameters
 * @param geoPoint Geographic point for geofencing
 * @param radius Radius in meters for geofencing
 * @param maxSpeed Maximum speed in km/h for speed control
 * @param inactivityMinutes Minutes of inactivity before alert
 */
data class RuleParameters(
    val geoPoint: GeoPoint? = null,
    val radius: Double = 100.0,
    val maxSpeed: Double = 80.0,
    val inactivityMinutes: Int = 30
)

