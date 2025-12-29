package pt.isec.a2022143267.safetysec.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import pt.isec.a2022143267.safetysec.model.MonitorProtectedRelation
import pt.isec.a2022143267.safetysec.model.RelationStatus
import pt.isec.a2022143267.safetysec.model.User

/**
 * Repository for User and Relation operations with Firestore
 */
class UserRepository {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    /**
     * Get user by ID
     */
    suspend fun getUserById(userId: String): Result<User> {
        return try {
            val userDoc = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            val user = userDoc.toObject(User::class.java)
                ?: throw Exception("User not found")

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Generate OTP for protected user
     */
    suspend fun generateOTP(protectedId: String): Result<String> {
        return try {
            val otp = (100000..999999).random().toString()
            val docRef = firestore.collection("otps").document()

            val otpData = hashMapOf(
                "otp" to otp,
                "protectedId" to protectedId,
                "createdAt" to com.google.firebase.Timestamp.now(),
                "used" to false
            )

            docRef.set(otpData).await()
            Result.success(otp)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Create relation request using OTP
     */
    suspend fun createRelationWithOTP(monitorId: String, otp: String): Result<String> {
        return try {
            // Find OTP document
            val otpQuery = firestore.collection("otps")
                .whereEqualTo("otp", otp)
                .whereEqualTo("used", false)
                .get()
                .await()

            if (otpQuery.documents.isEmpty()) {
                throw Exception("Invalid or expired OTP")
            }

            val otpDoc = otpQuery.documents.first()
            val protectedId = otpDoc.getString("protectedId")
                ?: throw Exception("Protected ID not found")

            // Create relation
            val relationRef = firestore.collection("relations").document()
            val relation = MonitorProtectedRelation(
                id = relationRef.id,
                monitorId = monitorId,
                protectedId = protectedId,
                otp = otp,
                status = RelationStatus.PENDING
            )

            relationRef.set(relation).await()

            // Mark OTP as used
            otpDoc.reference.update("used", true).await()

            Result.success(relationRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get pending relations for a protected user
     */
    fun getPendingRelations(protectedId: String): Flow<List<MonitorProtectedRelation>> = callbackFlow {
        val listener = firestore.collection("relations")
            .whereEqualTo("protectedId", protectedId)
            .whereEqualTo("status", RelationStatus.PENDING.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val relations = snapshot?.documents?.mapNotNull {
                    it.toObject(MonitorProtectedRelation::class.java)
                } ?: emptyList()

                trySend(relations)
            }

        awaitClose { listener.remove() }
    }

    /**
     * Approve or reject a relation
     */
    suspend fun updateRelationStatus(
        relationId: String,
        status: RelationStatus
    ): Result<Unit> {
        return try {
            firestore.collection("relations")
                .document(relationId)
                .update(
                    "status", status.name,
                    "respondedAt", com.google.firebase.Timestamp.now()
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get protected users for a monitor
     */
    fun getProtectedUsers(monitorId: String): Flow<List<User>> = callbackFlow {
        val listener = firestore.collection("relations")
            .whereEqualTo("monitorId", monitorId)
            .whereEqualTo("status", RelationStatus.APPROVED.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val protectedIds = snapshot?.documents?.mapNotNull {
                    it.getString("protectedId")
                } ?: emptyList()

                // Get user details for each protected ID
                if (protectedIds.isEmpty()) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                firestore.collection("users")
                    .whereIn("id", protectedIds)
                    .get()
                    .addOnSuccessListener { userSnapshot ->
                        val users = userSnapshot.documents.mapNotNull {
                            it.toObject(User::class.java)
                        }
                        trySend(users)
                    }
            }

        awaitClose { listener.remove() }
    }

    /**
     * Get monitors for a protected user
     */
    fun getMonitors(protectedId: String): Flow<List<User>> = callbackFlow {
        val listener = firestore.collection("relations")
            .whereEqualTo("protectedId", protectedId)
            .whereEqualTo("status", RelationStatus.APPROVED.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val monitorIds = snapshot?.documents?.mapNotNull {
                    it.getString("monitorId")
                } ?: emptyList()

                if (monitorIds.isEmpty()) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                firestore.collection("users")
                    .whereIn("id", monitorIds)
                    .get()
                    .addOnSuccessListener { userSnapshot ->
                        val users = userSnapshot.documents.mapNotNull {
                            it.toObject(User::class.java)
                        }
                        trySend(users)
                    }
            }

        awaitClose { listener.remove() }
    }

    /**
     * Remove monitor-protected relation
     */
    suspend fun removeMonitorRelation(protectedId: String, monitorId: String): Result<Unit> {
        return try {
            val querySnapshot = firestore.collection("relations")
                .whereEqualTo("protectedId", protectedId)
                .whereEqualTo("monitorId", monitorId)
                .get()
                .await()

            querySnapshot.documents.forEach { doc ->
                doc.reference.delete().await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
