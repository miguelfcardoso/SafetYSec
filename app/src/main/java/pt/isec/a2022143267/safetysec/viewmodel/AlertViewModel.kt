package pt.isec.a2022143267.safetysec.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pt.isec.a2022143267.safetysec.model.Alert
import pt.isec.a2022143267.safetysec.model.AlertStatus
import pt.isec.a2022143267.safetysec.repository.AlertRepository

/**
 * ViewModel for Alert operations
 */
class AlertViewModel : ViewModel() {
    private val alertRepository = AlertRepository()

    private val _currentAlert = MutableStateFlow<Alert?>(null)
    val currentAlert: StateFlow<Alert?> = _currentAlert.asStateFlow()

    private val _countdown = MutableStateFlow(10)
    val countdown: StateFlow<Int> = _countdown.asStateFlow()

    private val _isRecordingVideo = MutableStateFlow(false)
    val isRecordingVideo: StateFlow<Boolean> = _isRecordingVideo.asStateFlow()

    private val _alertState = MutableStateFlow<AlertOperationState>(AlertOperationState.Idle)
    val alertState: StateFlow<AlertOperationState> = _alertState.asStateFlow()

    fun createAlert(alert: Alert) {
        viewModelScope.launch {
            _alertState.value = AlertOperationState.Loading
            alertRepository.createAlert(alert)
                .onSuccess { alertId ->
                    val newAlert = alert.copy(id = alertId)
                    _currentAlert.value = newAlert
                    _alertState.value = AlertOperationState.Countdown
                    startCountdown(newAlert)
                }
                .onFailure { exception ->
                    _alertState.value = AlertOperationState.Error(
                        exception.message ?: "Failed to create alert"
                    )
                }
        }
    }

    fun createPanicAlert(protectedId: String) {
        viewModelScope.launch {
            try {
                _alertState.value = AlertOperationState.Loading

                // Create a panic alert
                val alert = Alert(
                    protectedId = protectedId,
                    monitorId = "", // Will be filled by the system for each monitor
                    ruleId = "panic_button_${System.currentTimeMillis()}",
                    alertType = pt.isec.a2022143267.safetysec.model.RuleType.PANIC_BUTTON,
                    status = AlertStatus.PENDING
                )

                alertRepository.createAlert(alert)
                    .onSuccess { alertId ->
                        val newAlert = alert.copy(id = alertId)
                        _currentAlert.value = newAlert
                        _alertState.value = AlertOperationState.Countdown
                        startCountdown(newAlert)
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

    private suspend fun startCountdown(alert: Alert) {
        for (i in 10 downTo 1) {
            _countdown.value = i
            delay(1000)

            // Check if alert was cancelled
            if (_alertState.value is AlertOperationState.Cancelled) {
                return
            }
        }

        // Countdown finished, activate alert and start recording
        activateAlert(alert)
    }

    private fun activateAlert(alert: Alert) {
        viewModelScope.launch {
            alertRepository.updateAlertStatus(alert.id, AlertStatus.ACTIVE)
                .onSuccess {
                    _alertState.value = AlertOperationState.Active
                    _isRecordingVideo.value = true
                    // Here you would trigger the video recording
                }
                .onFailure { exception ->
                    _alertState.value = AlertOperationState.Error(
                        exception.message ?: "Failed to activate alert"
                    )
                }
        }
    }

    fun cancelAlert(cancelledBy: String) {
        val alert = _currentAlert.value ?: return

        viewModelScope.launch {
            alertRepository.cancelAlert(alert, cancelledBy)
                .onSuccess {
                    _alertState.value = AlertOperationState.Cancelled
                    _countdown.value = 10
                    _isRecordingVideo.value = false
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
    }
}

sealed class AlertOperationState {
    object Idle : AlertOperationState()
    object Loading : AlertOperationState()
    object Countdown : AlertOperationState()
    object Active : AlertOperationState()
    object Cancelled : AlertOperationState()
    object VideoRecorded : AlertOperationState()
    data class Error(val message: String) : AlertOperationState()
}

