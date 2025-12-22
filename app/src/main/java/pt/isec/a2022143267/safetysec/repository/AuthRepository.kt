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
}

