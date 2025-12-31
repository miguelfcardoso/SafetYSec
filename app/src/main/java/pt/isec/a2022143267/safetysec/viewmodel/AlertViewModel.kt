package pt.isec.a2022143267.safetysec.viewmodel

import android.content.Context
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pt.isec.a2022143267.safetysec.model.Alert
import pt.isec.a2022143267.safetysec.model.AlertStatus
import pt.isec.a2022143267.safetysec.model.User
import pt.isec.a2022143267.safetysec.repository.AlertRepository
import pt.isec.a2022143267.safetysec.service.DirectNotificationService
import pt.isec.a2022143267.safetysec.utils.VideoRecordingHelper
import java.io.File

/**
 * ViewModel for Alert operations
 */
class AlertViewModel : ViewModel() {
    private val alertRepository = AlertRepository()
    private val notificationService = DirectNotificationService()

    private var videoRecordingHelper: VideoRecordingHelper? = null

    private val _currentAlert = MutableStateFlow<Alert?>(null)
    val currentAlert: StateFlow<Alert?> = _currentAlert.asStateFlow()

    private val _countdown = MutableStateFlow(10)
    val countdown: StateFlow<Int> = _countdown.asStateFlow()

    private val _isRecordingVideo = MutableStateFlow(false)
    val isRecordingVideo: StateFlow<Boolean> = _isRecordingVideo.asStateFlow()

    private val _alertState = MutableStateFlow<AlertOperationState>(AlertOperationState.Idle)
    val alertState: StateFlow<AlertOperationState> = _alertState.asStateFlow()

    private var isCancelled = false

    private var countdownJob: Job? = null

    fun initializeVideoRecording(context: Context, lifecycleOwner: LifecycleOwner) {
        videoRecordingHelper = VideoRecordingHelper(context, lifecycleOwner)
    }

    fun createAlert(alert: Alert, protectedUser: User) {
        viewModelScope.launch {
            _alertState.value = AlertOperationState.Loading
            isCancelled = false

            alertRepository.createAlert(alert)
                .onSuccess { alertId ->
                    val newAlert = alert.copy(id = alertId)
                    _currentAlert.value = newAlert
                    _alertState.value = AlertOperationState.Countdown(alertId)
                    startCountdown(newAlert, protectedUser)
                }
                .onFailure { exception ->
                    _alertState.value = AlertOperationState.Error(
                        exception.message ?: "Failed to create alert"
                    )
                }
        }
    }

    fun createPanicAlert(protectedId: String, protectedUser: User) {
        viewModelScope.launch {
            try {
                _alertState.value = AlertOperationState.Loading
                isCancelled = false

                // Create a panic alert
                val alert = Alert(
                    protectedId = protectedId,
                    monitorId = "", // Will be filled for each monitor
                    ruleId = "panic_button_${System.currentTimeMillis()}",
                    alertType = pt.isec.a2022143267.safetysec.model.RuleType.PANIC_BUTTON,
                    status = AlertStatus.PENDING
                )

                alertRepository.createAlert(alert)
                    .onSuccess { alertId ->
                        val newAlert = alert.copy(id = alertId)
                        _currentAlert.value = newAlert
                        _alertState.value = AlertOperationState.Countdown(alertId)
                        startCountdown(newAlert, protectedUser)
                    }
                    .onFailure { exception ->
                        _alertState.value = AlertOperationState.Error(
                            exception.message ?: "Failed to create panic alert"
                        )
                    }
            } catch (e: Exception) {
                _alertState.value = AlertOperationState.Error(
                    e.message ?: "Failed to create panic alert"
                )
            }
        }
    }

    private suspend fun startCountdown(alert: Alert, protectedUser: User) {
        for (i in 10 downTo 1) {
            _countdown.value = i
            delay(1000)

            // Check if alert was cancelled
            if (isCancelled) {
                _alertState.value = AlertOperationState.Cancelled
                return
            }
        }

        // Countdown finished, activate alert and start recording
        activateAlert(alert, protectedUser)
    }

    private fun stopCountdown() {
        countdownJob?.cancel()
        countdownJob = null
    }

    private fun activateAlert(alert: Alert, protectedUser: User) {
        viewModelScope.launch {
            alertRepository.updateAlertStatus(alert.id, AlertStatus.ACTIVE)
                .onSuccess {
                    _alertState.value = AlertOperationState.Active(alert.id)
                    _isRecordingVideo.value = true

                    // Send notifications to all monitors
                    sendAlertToMonitors(alert, protectedUser)
                }
                .onFailure { exception ->
                    _alertState.value = AlertOperationState.Error(
                        exception.message ?: "Failed to activate alert"
                    )
                }
        }
    }

    private suspend fun sendAlertToMonitors(alert: Alert, protectedUser: User) {
        try {
            // Send notification to all monitors using direct notification system
            notificationService.sendAlertToMonitors(alert, protectedUser.name)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Start video recording (called from UI when ready)
     */
    fun startVideoRecording(previewView: PreviewView) {
        val alert = _currentAlert.value ?: return
        val helper = videoRecordingHelper ?: return

        viewModelScope.launch {
            try {
                _isRecordingVideo.value = true

                // Record for 30 seconds
                val videoFile = helper.startRecording(previewView, durationSeconds = 30)

                if (videoFile != null) {
                    // Upload video to Firebase Storage
                    uploadVideoAndNotify(alert, videoFile)
                } else {
                    _isRecordingVideo.value = false
                    // Alert sent but without video
                    _alertState.value = AlertOperationState.VideoRecorded
                }
            } catch (e: Exception) {
                _isRecordingVideo.value = false
                // Continue without video
                _alertState.value = AlertOperationState.VideoRecorded
            }
        }
    }

    private suspend fun uploadVideoAndNotify(alert: Alert, videoFile: File) {
        try {
            val videoUrl = videoRecordingHelper?.uploadVideoToFirebase(
                videoFile,
                alert.protectedId,
                alert.id
            )

            if (videoUrl != null) {
                alertRepository.updateAlertVideoUrl(alert.id, videoUrl)
                    .onSuccess {
                        _isRecordingVideo.value = false
                        _alertState.value = AlertOperationState.VideoRecorded
                    }
                    .onFailure {
                        _isRecordingVideo.value = false
                        _alertState.value = AlertOperationState.VideoRecorded
                    }
            }
        } catch (e: Exception) {
            _isRecordingVideo.value = false
            _alertState.value = AlertOperationState.VideoRecorded
        }
    }

    fun cancelAlert(inputCode: String, correctCode: String, cancelledBy: String) {
        if (inputCode != correctCode) {
            _alertState.value = AlertOperationState.Error("Código de cancelamento incorreto")
            return
        }

        val alert = _currentAlert.value ?: return
        isCancelled = true

        viewModelScope.launch {
            alertRepository.cancelAlert(alert, cancelledBy)
                .onSuccess {
                    _alertState.value = AlertOperationState.Cancelled
                    _countdown.value = 10
                    _isRecordingVideo.value = false
                    stopCountdown()
                }
                .onFailure { exception ->
                    _alertState.value = AlertOperationState.Error(
                        exception.message ?: "Failed to cancel alert"
                    )
                }
        }
    }

    fun updateVideoUrl(alertId: String, videoUrl: String) {
        viewModelScope.launch {
            alertRepository.updateAlertVideoUrl(alertId, videoUrl)
                .onSuccess {
                    _isRecordingVideo.value = false
                    _alertState.value = AlertOperationState.VideoRecorded
                }
                .onFailure { exception ->
                    _alertState.value = AlertOperationState.Error(
                        exception.message ?: "Failed to update video URL"
                    )
                }
        }
    }

    fun resetAlert() {
        _currentAlert.value = null
        _countdown.value = 10
        _isRecordingVideo.value = false
        _alertState.value = AlertOperationState.Idle
        isCancelled = false
    }
}

sealed class AlertOperationState {
    object Idle : AlertOperationState()
    object Loading : AlertOperationState()
    data class Countdown(val alertId: String) : AlertOperationState()
    data class Active(val alertId: String) : AlertOperationState()
    object Cancelled : AlertOperationState()
    object VideoRecorded : AlertOperationState()
    data class Error(val message: String) : AlertOperationState()
}

