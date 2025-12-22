package pt.isec.a2022143267.safetysec.utils

import android.location.Location
import com.google.firebase.firestore.GeoPoint
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Utility functions for location calculations
 */
object LocationUtils {

    /**
     * Calculate distance between two GeoPoints in meters
     */
    fun calculateDistance(point1: GeoPoint, point2: GeoPoint): Double {
        val earthRadius = 6371000.0 // meters

        val lat1Rad = Math.toRadians(point1.latitude)
        val lat2Rad = Math.toRadians(point2.latitude)
        val deltaLat = Math.toRadians(point2.latitude - point1.latitude)
        val deltaLng = Math.toRadians(point2.longitude - point1.longitude)

        val a = sin(deltaLat / 2) * sin(deltaLat / 2) +
                cos(lat1Rad) * cos(lat2Rad) *
                sin(deltaLng / 2) * sin(deltaLng / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadius * c
    }

    /**
     * Check if a location is inside a geofence
     */
    fun isInsideGeofence(
        currentLocation: Location,
        centerPoint: GeoPoint,
        radiusMeters: Double
    ): Boolean {
        val currentGeoPoint = GeoPoint(currentLocation.latitude, currentLocation.longitude)
        val distance = calculateDistance(currentGeoPoint, centerPoint)
        return distance <= radiusMeters
    }

    /**
     * Convert Location to GeoPoint
     */
    fun locationToGeoPoint(location: Location): GeoPoint {
        return GeoPoint(location.latitude, location.longitude)
    }
}

