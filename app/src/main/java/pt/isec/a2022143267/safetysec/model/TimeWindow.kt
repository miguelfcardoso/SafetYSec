package pt.isec.a2022143267.safetysec.model

import com.google.firebase.Timestamp

/**
 * Time Window data class
 * Defines when monitoring rules should be active
 * @param id Time window unique identifier
 * @param protectedId Protected user ID
 * @param name Window name
 * @param startHour Start hour (0-23)
 * @param startMinute Start minute (0-59)
 * @param endHour End hour (0-23)
 * @param endMinute End minute (0-59)
 * @param daysOfWeek Days when this window is active (1=Monday, 7=Sunday)
 * @param isEnabled Whether this window is active
 * @param createdAt Creation timestamp
 */
data class TimeWindow(
    val id: String = "",
    val protectedId: String = "",
    val name: String = "",
    val startHour: Int = 0,
    val startMinute: Int = 0,
    val endHour: Int = 23,
    val endMinute: Int = 59,
    val daysOfWeek: List<Int> = listOf(1, 2, 3, 4, 5, 6, 7),
    val isEnabled: Boolean = true,
    val createdAt: Timestamp = Timestamp.now()
)

