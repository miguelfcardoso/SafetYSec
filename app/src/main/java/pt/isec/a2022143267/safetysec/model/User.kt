package pt.isec.a2022143267.safetysec.model

import com.google.firebase.Timestamp

/**
 * User data class
 * @param id User unique identifier
 * @param name User name
 * @param email User email
 * @param userType User type (MONITOR or PROTECTED)
 * @param createdAt Account creation timestamp
 * @param cancelCode Cancellation code for protected users
 */
data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val userType: UserType = UserType.PROTECTED,
    val createdAt: Timestamp = Timestamp.now(),
    val cancelCode: String = ""
)

enum class UserType {
    MONITOR,
    PROTECTED
}

