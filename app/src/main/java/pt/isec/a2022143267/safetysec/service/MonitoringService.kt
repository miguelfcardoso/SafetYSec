package pt.isec.a2022143267.safetysec.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import pt.isec.a2022143267.safetysec.MainActivity
import pt.isec.a2022143267.safetysec.R
import pt.isec.a2022143267.safetysec.model.*
import pt.isec.a2022143267.safetysec.utils.DateTimeUtils
import pt.isec.a2022143267.safetysec.utils.LocationUtils
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Foreground service for monitoring sensors and location
 */
class MonitoringService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var lastUpdateTime: Long = System.currentTimeMillis()
    private var activeTimeWindows = mutableListOf<TimeWindow>()
    private var lastLocation: Location? = null
    private var currentLocation: Location? = null

    // Detection thresholds
    private val fallAccelerationThreshold = 25.0f // m/s² for fall detection
    private val accidentDecelerationThreshold = -15.0f // m/s² for accident (negative = deceleration)
    private val inactivityThresholdMillis = 30 * 60 * 1000L // 30 minutes default

    private var previousAcceleration = 0f
    private var activeRules = mutableListOf<Rule>()

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "monitoring_channel"
        private const val CHANNEL_NAME = "Monitoring Service"
        const val ACTION_PANIC = "pt.isec.a2022143267.safetysec.PANIC"

        fun start(context: Context) {
            val intent = Intent(context, MonitoringService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, MonitoringService::class.java)
            context.stopService(intent)
        }

        fun triggerPanic(context: Context) {
            val intent = Intent(context, MonitoringService::class.java).apply {
                action = ACTION_PANIC
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())

        // Initialize sensors
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        accelerometer?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }

        // Initialize location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setupLocationUpdates()

        // Load active rules
        loadActiveRules()

        // Start inactivity checker
        startInactivityChecker()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_PANIC) {
            handlePanicButton()
        }
        return START_STICKY
    }

    private fun loadActiveRules() {
        val userId = auth.currentUser?.uid ?: return

        serviceScope.launch {
            try {
                firestore.collection("timeWindows")
                    .whereEqualTo("protectedId", userId)
                    .whereEqualTo("isEnabled", true)
                    .addSnapshotListener { snapshot, _ ->
                        activeTimeWindows = snapshot?.toObjects(TimeWindow::class.java) ?: mutableListOf()
                    }
                firestore.collection("rules")
                    .whereEqualTo("protectedId", userId)
                    .whereEqualTo("isEnabled", true)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) return@addSnapshotListener

                        activeRules.clear()
                        snapshot?.documents?.forEach { doc ->
                            doc.toObject(Rule::class.java)?.let { rule ->
                                if (isInTimeWindow()) {
                                    activeRules.add(rule)
                                }
                            }
                        }
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun isInTimeWindow(): Boolean {
        if (activeTimeWindows.isEmpty()) return true

        return activeTimeWindows.any { window ->
            DateTimeUtils.isNowInTimeWindow(window)
        }
    }

    private fun setupLocationUpdates() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            10000L // 10 seconds
        ).apply {
            setMinUpdateIntervalMillis(5000L) // 5 seconds
        }.build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                if (!isInTimeWindow()) return

                lastLocation = currentLocation
                currentLocation = locationResult.lastLocation
                checkSpeedAndGeofencing()
                lastUpdateTime = System.currentTimeMillis()
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null
            )
        } catch (e: SecurityException) {
            // Handle permission not granted
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (!isInTimeWindow()) return

        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            // Calculate total acceleration
            val acceleration = sqrt(x * x + y * y + z * z)

            // Detect fall (sudden HIGH acceleration)
            if (acceleration > fallAccelerationThreshold) {
                detectFall()
            }

            // Detect accident (sudden DECELERATION)
            val accelerationChange = acceleration - previousAcceleration
            if (accelerationChange < accidentDecelerationThreshold) {
                detectAccident(abs(accelerationChange))
            }

            previousAcceleration = acceleration
            lastUpdateTime = System.currentTimeMillis()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed for this implementation
    }

    private fun detectFall() {
        val rule = activeRules.find { it.ruleType == RuleType.FALL_DETECTION } ?: return
        triggerAlert(rule, RuleType.FALL_DETECTION, emptyMap())
    }

    private fun detectAccident(deceleration: Float) {
        val rule = activeRules.find { it.ruleType == RuleType.ACCIDENT } ?: return
        val data = mapOf("deceleration" to deceleration.toString())
        triggerAlert(rule, RuleType.ACCIDENT, data)
    }

    private fun handlePanicButton() {
        val userId = auth.currentUser?.uid ?: return
        val rule = activeRules.find { it.ruleType == RuleType.PANIC_BUTTON }
            ?: Rule(
                id = "panic_${System.currentTimeMillis()}",
                protectedId = userId,
                ruleType = RuleType.PANIC_BUTTON,
                name = "Panic Button",
                isEnabled = true
            )

        triggerAlert(rule, RuleType.PANIC_BUTTON, emptyMap())
    }

    private fun checkSpeedAndGeofencing() {
        currentLocation?.let { location ->
            // Check speed
            val speedKmH = location.speed * 3.6 // Convert m/s to km/h
            activeRules.filter { it.ruleType == RuleType.SPEED_CONTROL }.forEach { rule ->
                if (speedKmH > rule.parameters.maxSpeed) {
                    val data = mapOf("speed" to speedKmH.toString())
                    triggerAlert(rule, RuleType.SPEED_CONTROL, data)
                }
            }

            // Check geofencing
            activeRules.filter { it.ruleType == RuleType.GEOFENCING }.forEach { rule ->
                rule.parameters.geoPoint?.let { center ->
                    val isInside = LocationUtils.isInsideGeofence(
                        location,
                        center,
                        rule.parameters.radius
                    )

                    if (!isInside) {
                        val distance = LocationUtils.calculateDistance(
                            GeoPoint(location.latitude, location.longitude),
                            center
                        )
                        val data = mapOf("distance" to distance.toString())
                        triggerAlert(rule, RuleType.GEOFENCING, data)
                    }
                }
            }
        }
    }

    private fun startInactivityChecker() {
        serviceScope.launch {
            while (isActive) {
                delay(60000) // Check every minute

                val timeSinceLastUpdate = System.currentTimeMillis() - lastUpdateTime

                activeRules.filter { it.ruleType == RuleType.INACTIVITY }.forEach { rule ->
                    val thresholdMillis = rule.parameters.inactivityMinutes * 60 * 1000L

                    if (timeSinceLastUpdate > thresholdMillis) {
                        val data = mapOf("inactiveMinutes" to (timeSinceLastUpdate / 60000).toString())
                        triggerAlert(rule, RuleType.INACTIVITY, data)
                    }
                }
            }
        }
    }

    private fun triggerAlert(rule: Rule, type: RuleType, additionalData: Map<String, String>) {
        serviceScope.launch {
            try {
                val userId = auth.currentUser?.uid ?: return@launch

                // Get monitors for this user
                val relationsSnapshot = firestore.collection("relations")
                    .whereEqualTo("protectedId", userId)
                    .whereEqualTo("status", RelationStatus.APPROVED.name)
                    .get()
                    .await()

                if (relationsSnapshot.documents.isEmpty()) {
                    // No monitors, no need to create alert
                    return@launch
                }

                // Create alert for each monitor
                relationsSnapshot.documents.forEach { doc ->
                    val monitorId = doc.getString("monitorId") ?: return@forEach

                    val alert = Alert(
                        protectedId = userId,
                        monitorId = monitorId,
                        ruleId = rule.id,
                        alertType = type,
                        status = AlertStatus.PENDING,
                        location = currentLocation?.let {
                            GeoPoint(it.latitude, it.longitude)
                        },
                        additionalData = additionalData
                    )

                    // Create alert in Firebase
                    val alertRef = firestore.collection("alerts").document()
                    alertRef.set(alert.copy(id = alertRef.id)).await()

                    // Notify the protected user (only once, not for each monitor)
                    if (doc == relationsSnapshot.documents.first()) {
                        sendAlertNotificationToProtected(alertRef.id, type)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Send notification to protected user giving them 10 seconds to cancel
     */
    private fun sendAlertNotificationToProtected(alertId: String, type: RuleType) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("alertId", alertId)
            putExtra("showAlertScreen", true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            alertId.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("⚠️ Safety Alert Detected!")
            .setContentText("${type.name.replace("_", " ")} - You have 10 seconds to cancel")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(pendingIntent, true) // Show as full screen for urgent alerts
            .build()

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(alertId.hashCode(), notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Monitoring active rules"
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification() = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle(getString(R.string.app_name))
        .setContentText("Monitoring is active")
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentIntent(
            PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE
            )
        )
        .build()

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        fusedLocationClient.removeLocationUpdates(locationCallback)
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

