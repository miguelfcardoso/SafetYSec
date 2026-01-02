package pt.isec.a2022143267.safetysec.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import pt.isec.a2022143267.safetysec.model.User
import pt.isec.a2022143267.safetysec.model.UserType

/**
 * Repository for authentication operations with Firebase
 */
class AuthRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    /**
     * Register a new user
     */
    suspend fun register(
        email: String,
        password: String,
        name: String,
        userType: UserType
    ): Result<User> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid ?: throw Exception("User ID not found")

            val cancelCode = if (userType == UserType.PROTECTED) {
                generateCancelCode()
            } else ""

            val user = User(
                id = userId,
                name = name,
                email = email,
                userType = userType,
                cancelCode = cancelCode
            )

            firestore.collection("users")
                .document(userId)
                .set(user)
                .await()

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Login user
     */
    suspend fun login(email: String, password: String): Result<User> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid ?: throw Exception("User ID not found")

            val userDoc = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            val user = userDoc.toObject(User::class.java)
                ?: throw Exception("User data not found")

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserField(userId: String, field: String, value: String): Result<Unit> {
        return try {
            firestore.collection("users").document(userId)
                .update(field, value)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Logout current user
     */
    fun logout() {
        auth.signOut()
    }

    /**
     * Reset password
     */
    suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePassword(password: String): Result<Unit> {
        return try {
            auth.currentUser?.updatePassword(password)?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get current user data
     */
    suspend fun getCurrentUser(): Result<User> {
        return try {
            val userId = currentUser?.uid ?: throw Exception("No user logged in")

            val userDoc = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            val user = userDoc.toObject(User::class.java)
                ?: throw Exception("User data not found")

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Generate a random cancellation code
     */
    private fun generateCancelCode(): String {
        return (1000..9999).random().toString()
    }

    /**
     * Generate and store OTP for MFA
     * @return 6-digit OTP code
     */
    suspend fun generateAndStoreOTP(userId: String): Result<String> {
        return try {
            val otp = (100000..999999).random().toString()
            val expirationTime = System.currentTimeMillis() + (10 * 60 * 1000) // 10 minutes

            val otpData = mapOf(
                "otp" to otp,
                "expiresAt" to expirationTime,
                "attempts" to 0
            )

            firestore.collection("users")
                .document(userId)
                .update("mfaData", otpData)
                .await()

            Result.success(otp)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Verify OTP code
     * @return true if OTP is valid and not expired
     */
    suspend fun verifyOTP(userId: String, enteredOTP: String): Result<Boolean> {
        return try {
            val userDoc = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            @Suppress("UNCHECKED_CAST")
            val mfaData = userDoc.get("mfaData") as? Map<String, Any>

            if (mfaData == null) {
                return Result.success(false)
            }

            val storedOTP = mfaData["otp"] as? String
            val expiresAt = mfaData["expiresAt"] as? Long ?: 0L
            val attempts = (mfaData["attempts"] as? Long)?.toInt() ?: 0

            // Check if OTP is expired
            if (System.currentTimeMillis() > expiresAt) {
                return Result.failure(Exception("OTP expired. Please request a new one."))
            }

            // Check maximum attempts (5 attempts)
            if (attempts >= 5) {
                return Result.failure(Exception("Maximum attempts exceeded. Please request a new OTP."))
            }

            // Verify OTP
            if (storedOTP == enteredOTP) {
                // Clear OTP data after successful verification
                firestore.collection("users")
                    .document(userId)
                    .update("mfaData", null)
                    .await()
                Result.success(true)
            } else {
                // Increment attempts
                firestore.collection("users")
                    .document(userId)
                    .update("mfaData.attempts", attempts + 1)
                    .await()
                Result.success(false)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Generate OTP for Monitor-Protected association
     * @return 6-digit OTP code that expires in 10 minutes
     */
    suspend fun generateAssociationOTP(monitorId: String): Result<String> {
        return try {
            val otp = (100000..999999).random().toString()
            val expirationTime = System.currentTimeMillis() + (10 * 60 * 1000) // 10 minutes

            val otpData = mapOf(
                "associationOTP" to otp,
                "otpExpiresAt" to expirationTime
            )

            firestore.collection("users")
                .document(monitorId)
                .update(otpData)
                .await()

            Result.success(otp)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Verify association OTP
     */
    suspend fun verifyAssociationOTP(monitorId: String, enteredOTP: String): Result<Boolean> {
        return try {
            val userDoc = firestore.collection("users")
                .document(monitorId)
                .get()
                .await()

            val storedOTP = userDoc.getString("associationOTP")
            val expiresAt = userDoc.getLong("otpExpiresAt") ?: 0L

            // Check if OTP exists
            if (storedOTP.isNullOrEmpty()) {
                return Result.failure(Exception("No OTP found. Please request one from the Monitor."))
            }

            // Check if OTP is expired
            if (System.currentTimeMillis() > expiresAt) {
                return Result.failure(Exception("OTP expired. Please request a new one."))
            }

            // Verify OTP
            if (storedOTP == enteredOTP) {
                // Clear OTP after successful verification
                firestore.collection("users")
                    .document(monitorId)
                    .update(mapOf(
                        "associationOTP" to null,
                        "otpExpiresAt" to null
                    ))
                    .await()
                Result.success(true)
            } else {
                Result.success(false)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

