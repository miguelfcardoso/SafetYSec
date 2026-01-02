package pt.isec.a2022143267.safetysec.utils

import pt.isec.a2022143267.safetysec.model.TimeWindow
import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility functions for date and time formatting
 */
object DateTimeUtils {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    fun formatDate(timestamp: com.google.firebase.Timestamp): String {
        return dateFormat.format(timestamp.toDate())
    }

    fun formatTime(timestamp: com.google.firebase.Timestamp): String {
        return timeFormat.format(timestamp.toDate())
    }

    fun formatDateTime(timestamp: com.google.firebase.Timestamp): String {
        return dateTimeFormat.format(timestamp.toDate())
    }

    fun isCurrentlyInTimeWindow(
        startHour: Int,
        startMinute: Int,
        endHour: Int,
        endMinute: Int,
        daysOfWeek: List<Int>
    ): Boolean {
        val now = Calendar.getInstance()
        val currentDayOfWeek = now.get(Calendar.DAY_OF_WEEK)

        // Check if today is in the list of active days (1=Monday, 7=Sunday)
        val mondayBasedDay = if (currentDayOfWeek == Calendar.SUNDAY) 7 else currentDayOfWeek - 1
        if (!daysOfWeek.contains(mondayBasedDay)) {
            return false
        }

        // Check if current time is within the window
        val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        val startMinutes = startHour * 60 + startMinute
        val endMinutes = endHour * 60 + endMinute

        return if (endMinutes > startMinutes) {
            currentMinutes in startMinutes..endMinutes
        } else {
            // Window spans midnight
            currentMinutes >= startMinutes || currentMinutes <= endMinutes
        }
    }

    fun isNowInTimeWindow(window: TimeWindow): Boolean {
        val now = Calendar.getInstance()
        val dayOfWeek = now.get(Calendar.DAY_OF_WEEK) // 1 = Domingo, 2 = Segunda...

        // Converter o dia da semana do Calendar para o formato do seu modelo (1=Seg, 7=Dom)
        val convertedDay = if (dayOfWeek == Calendar.SUNDAY) 7 else dayOfWeek - 1

        if (!window.daysOfWeek.contains(convertedDay)) return false

        val nowHour = now.get(Calendar.HOUR_OF_DAY)
        val nowMinute = now.get(Calendar.MINUTE)

        val startMinutes = window.startHour * 60 + window.startMinute
        val endMinutes = window.endHour * 60 + window.endMinute
        val currentMinutes = nowHour * 60 + nowMinute

        return currentMinutes in startMinutes..endMinutes
    }

    /**
     * Format a timestamp as relative time (e.g., "5 minutes ago", "2 hours ago")
     */
    fun formatRelativeTime(timestamp: com.google.firebase.Timestamp): String {
        val now = System.currentTimeMillis()
        val then = timestamp.toDate().time
        val diff = now - then

        return when {
            diff < 60 * 1000 -> "just now"
            diff < 60 * 60 * 1000 -> {
                val minutes = (diff / (60 * 1000)).toInt()
                "$minutes minute${if (minutes != 1) "s" else ""} ago"
            }
            diff < 24 * 60 * 60 * 1000 -> {
                val hours = (diff / (60 * 60 * 1000)).toInt()
                "$hours hour${if (hours != 1) "s" else ""} ago"
            }
            diff < 7 * 24 * 60 * 60 * 1000 -> {
                val days = (diff / (24 * 60 * 60 * 1000)).toInt()
                "$days day${if (days != 1) "s" else ""} ago"
            }
            else -> formatDateTime(timestamp)
        }
    }
}
