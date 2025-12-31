package pt.isec.a2022143267.safetysec.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pt.isec.a2022143267.safetysec.model.Alert
import pt.isec.a2022143267.safetysec.model.MonitorProtectedRelation
import pt.isec.a2022143267.safetysec.model.RelationStatus
import pt.isec.a2022143267.safetysec.model.Rule
import pt.isec.a2022143267.safetysec.model.TimeWindow
import pt.isec.a2022143267.safetysec.model.User
import pt.isec.a2022143267.safetysec.repository.AlertRepository
import pt.isec.a2022143267.safetysec.repository.RuleRepository
import pt.isec.a2022143267.safetysec.repository.UserRepository
import pt.isec.a2022143267.safetysec.viewmodel.OperationState

/**
 * ViewModel for Protected user operations
 */
class ProtectedViewModel : ViewModel() {
    private val userRepository = UserRepository()
    private val ruleRepository = RuleRepository()
    private val alertRepository = AlertRepository()

    private val _monitors = MutableStateFlow<List<User>>(emptyList())
    val monitors: StateFlow<List<User>> = _monitors.asStateFlow()

    private val _rules = MutableStateFlow<List<Rule>>(emptyList())
    val rules: StateFlow<List<Rule>> = _rules.asStateFlow()

    private val _timeWindows = MutableStateFlow<List<TimeWindow>>(emptyList())
    val timeWindows: StateFlow<List<TimeWindow>> = _timeWindows.asStateFlow()

    private val _pendingRelations = MutableStateFlow<List<MonitorProtectedRelation>>(emptyList())
    val pendingRelations: StateFlow<List<MonitorProtectedRelation>> = _pendingRelations.asStateFlow()

    private val _alerts = MutableStateFlow<List<Alert>>(emptyList())
    val alerts: StateFlow<List<Alert>> = _alerts.asStateFlow()

    private val _operationState = MutableStateFlow<OperationState>(OperationState.Idle)
    val operationState: StateFlow<OperationState> = _operationState.asStateFlow()

    private val _generatedOTP = MutableStateFlow<String?>(null)
    val generatedOTP: StateFlow<String?> = _generatedOTP.asStateFlow()

    fun loadMonitors(protectedId: String) {
        viewModelScope.launch {
            userRepository.getMonitors(protectedId).collect { users ->
                _monitors.value = users
            }
        }
    }

    fun loadRules(protectedId: String) {
        viewModelScope.launch {
            ruleRepository.getRulesForProtected(protectedId).collect { rules ->
                _rules.value = rules
            }
        }
    }

    fun loadTimeWindows(protectedId: String) {
        viewModelScope.launch {
            ruleRepository.getTimeWindows(protectedId).collect { windows ->
                _timeWindows.value = windows
            }
        }
    }

    fun loadPendingRelations(protectedId: String) {
        viewModelScope.launch {
            userRepository.getPendingRelations(protectedId).collect { relations ->
                _pendingRelations.value = relations
            }
        }
    }

    fun loadAlerts(protectedId: String) {
        viewModelScope.launch {
            alertRepository.getAlertsForProtected(protectedId).collect { alerts ->
                _alerts.value = alerts
            }
        }
    }

    fun generateOTP(protectedId: String) {
        viewModelScope.launch {
            _operationState.value = OperationState.Loading
            userRepository.generateOTP(protectedId)
                .onSuccess { otp ->
                    _generatedOTP.value = otp
                    _operationState.value = OperationState.Success("OTP generated: $otp")
                }
                .onFailure { exception ->
                    _operationState.value = OperationState.Error(
                        exception.message ?: "Failed to generate OTP"
                    )
                }
        }
    }

    fun approveRelation(relationId: String) {
        viewModelScope.launch {
            _operationState.value = OperationState.Loading
            userRepository.updateRelationStatus(relationId, RelationStatus.APPROVED)
                .onSuccess {
                    _operationState.value = OperationState.Success("Monitor approved")
                }
                .onFailure { exception ->
                    _operationState.value = OperationState.Error(
                        exception.message ?: "Failed to approve relation"
                    )
                }
        }
    }

    fun rejectRelation(relationId: String) {
        viewModelScope.launch {
            _operationState.value = OperationState.Loading
            userRepository.updateRelationStatus(relationId, RelationStatus.REJECTED)
                .onSuccess {
                    _operationState.value = OperationState.Success("Monitor rejected")
                }
                .onFailure { exception ->
                    _operationState.value = OperationState.Error(
                        exception.message ?: "Failed to reject relation"
                    )
                }
        }
    }

    fun createTimeWindow(timeWindow: TimeWindow) {
        viewModelScope.launch {
            _operationState.value = OperationState.Loading
            ruleRepository.createTimeWindow(timeWindow)
                .onSuccess {
                    _operationState.value = OperationState.Success("Time window created")
                }
                .onFailure { exception ->
                    _operationState.value = OperationState.Error(
                        exception.message ?: "Failed to create time window"
                    )
                }
        }
    }

    fun deleteTimeWindow(windowId: String) {
        viewModelScope.launch {
            _operationState.value = OperationState.Loading
            ruleRepository.deleteTimeWindow(windowId)
                .onSuccess {
                    _operationState.value = OperationState.Success("Time window deleted")
                }
                .onFailure { exception ->
                    _operationState.value = OperationState.Error(
                        exception.message ?: "Failed to delete time window"
                    )
                }
        }
    }

    fun resetOperationState() {
        _operationState.value = OperationState.Idle
    }

    fun clearOTP() {
        _generatedOTP.value = null
    }

    fun removeMonitor(protectedId: String, monitorId: String) {
        viewModelScope.launch {
            _operationState.value = OperationState.Loading
            userRepository.removeMonitorRelation(protectedId, monitorId)
                .onSuccess {
                    _operationState.value = OperationState.Success("Monitor removed")
                }
                .onFailure { exception ->
                    _operationState.value = OperationState.Error(
                        exception.message ?: "Failed to remove monitor"
                    )
                }
        }
    }

    fun updateRuleStatus(ruleId: String, isEnabled: Boolean) {
        viewModelScope.launch {
            _operationState.value = OperationState.Loading
            ruleRepository.updateRuleStatus(ruleId, isEnabled)
                .onSuccess {
                    _operationState.value = OperationState.Success(
                        if (isEnabled) "Rule activated" else "Rule deactivated"
                    )
                }
                .onFailure { exception ->
                    _operationState.value = OperationState.Error(
                        exception.message ?: "Failed to update rule"
                    )
                }
        }
    }

    fun revokeRule(ruleId: String) {
        viewModelScope.launch {
            _operationState.value = OperationState.Loading
            ruleRepository.deleteRule(ruleId)
                .onSuccess {
                    _rules.value = _rules.value.filter { it.id != ruleId }

                    _operationState.value = OperationState.Success("Rule revoked")
                }
                .onFailure { exception ->
                    _operationState.value = OperationState.Error(
                        exception.message ?: "Failed to revoke rule"
                    )
                }
        }
    }

    fun deleteAlert(alertId: String) {
        viewModelScope.launch {
            _operationState.value = OperationState.Loading
            alertRepository.deleteAlert(alertId)
                .onSuccess {
                    _operationState.value = OperationState.Success("Alert deleted")
                }
                .onFailure { exception ->
                    _operationState.value = OperationState.Error(
                        exception.message ?: "Failed to delete alert"
                    )
                }
        }
    }
}
