package pt.isec.a2022143267.safetysec.model

import com.google.firebase.Timestamp

/**
 * MonitorProtectedRelation data class
 * Represents the relationship between a monitor and a protected user
 * @param id Relation unique identifier
 * @param monitorId Monitor user ID
 * @param protectedId Protected user ID
 * @param status Relation status (PENDING, APPROVED, REJECTED)
 * @param requestedAt When the relation was requested
 * @param respondedAt When the protected user responded
 * @param otp One-Time Password used for the association
 */
data class MonitorProtectedRelation(
    val id: String = "",
    val monitorId: String = "",
    val protectedId: String = "",
    val status: RelationStatus = RelationStatus.PENDING,
    val requestedAt: Timestamp = Timestamp.now(),
    val respondedAt: Timestamp? = null,
    val otp: String = ""
)

enum class RelationStatus {
    PENDING,    // Waiting for protected user approval
    APPROVED,   // Protected user approved the relation
    REJECTED    // Protected user rejected the relation
}

