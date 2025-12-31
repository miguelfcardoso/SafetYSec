package pt.isec.a2022143267.safetysec.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pt.isec.a2022143267.safetysec.model.Alert
import pt.isec.a2022143267.safetysec.model.AlertStatus
import pt.isec.a2022143267.safetysec.model.Rule
import pt.isec.a2022143267.safetysec.model.RuleType
import pt.isec.a2022143267.safetysec.model.User
import pt.isec.a2022143267.safetysec.repository.AlertRepository
import pt.isec.a2022143267.safetysec.repository.RuleRepository
import pt.isec.a2022143267.safetysec.repository.UserRepository

/**
 * ViewModel for Monitor operations
 */
class MonitorViewModel : ViewModel() {
    private val userRepository = UserRepository()
    private val ruleRepository = RuleRepository()
    private val alertRepository = AlertRepository()

    private val _protectedUsers = MutableStateFlow<List<User>>(emptyList())
    val protectedUsers: StateFlow<List<User>> = _protectedUsers.asStateFlow()

    private val _rules = MutableStateFlow<List<Rule>>(emptyList())
    val rules: StateFlow<List<Rule>> = _rules.asStateFlow()

    private val _alerts = MutableStateFlow<List<Alert>>(emptyList())
    val alerts: StateFlow<List<Alert>> = _alerts.asStateFlow()

    private val _activeAlerts = MutableStateFlow<List<Alert>>(emptyList())
    val activeAlerts: StateFlow<List<Alert>> = _activeAlerts.asStateFlow()

    private val _operationState = MutableStateFlow<OperationState>(OperationState.Idle)
    val operationState: StateFlow<OperationState> = _operationState.asStateFlow()

    private val _alertStats = MutableStateFlow<List<AlertStats>>(emptyList())
    val alertStats: StateFlow<List<AlertStats>> = _alertStats.asStateFlow()

    fun loadProtectedUsers(monitorId: String) {
        viewModelScope.launch {
            userRepository.getProtectedUsers(monitorId).collect { users ->
                _protectedUsers.value = users
            }
        }
    }

    fun loadRules(monitorId: String) {
        viewModelScope.launch {
            ruleRepository.getRulesForMonitor(monitorId).collect { rules ->
                _rules.value = rules
            }
        }
    }

    fun loadRulesForProtected(protectedId: String) {
        viewModelScope.launch {
            ruleRepository.getRulesForProtected(protectedId).collect { rules ->
                _rules.value = rules
            }
        }
    }

    fun toggleRule(ruleId: String, enabled: Boolean) {
        viewModelScope.launch {
            ruleRepository.toggleRule(ruleId, enabled)
                .onSuccess {
                    _operationState.value = OperationState.Success(
                        if (enabled) "Rule activated" else "Rule deactivated"
                    )
                }
                .onFailure { exception ->
                    _operationState.value = OperationState.Error(
                        exception.message ?: "Failed to toggle rule"
                    )
                }
        }
    }

    fun loadAlerts(monitorId: String) {
        viewModelScope.launch {
            alertRepository.getAlertsForMonitor(monitorId).collect { alerts ->
                _alerts.value = alerts
                calculateStats()
            }
        }
    }

    fun loadActiveAlerts(monitorId: String) {
        viewModelScope.launch {
            alertRepository.getActiveAlertsForMonitor(monitorId).collect { alerts ->
                _activeAlerts.value = alerts
            }
        }
    }

    fun calculateStats() {
        val currentAlerts = _alerts.value.filter { it.status != AlertStatus.CANCELLED }
        if (currentAlerts.isEmpty()) return

        val total = currentAlerts.size
        val stats = currentAlerts
            .groupBy { it.alertType }
            .map { (type, list) ->
                AlertStats(
                    type = type,
                    count = list.size,
                    percentage = list.size.toFloat() / total
                )
            }
            .sortedByDescending { it.count }

        _alertStats.value = stats
    }

    fun addProtectedUserWithOTP(monitorId: String, otp: String) {
        viewModelScope.launch {
            _operationState.value = OperationState.Loading
            userRepository.createRelationWithOTP(monitorId, otp)
                .onSuccess {
                    _operationState.value = OperationState.Success("Request sent successfully")
                }
                .onFailure { exception ->
                    _operationState.value = OperationState.Error(
                        exception.message ?: "Failed to add protected user"
                    )
                }
        }
    }

    fun createRule(rule: Rule) {
        viewModelScope.launch {
            _operationState.value = OperationState.Loading
            ruleRepository.createRule(rule)
                .onSuccess {
                    _operationState.value = OperationState.Success("Rule created successfully")
                }
                .onFailure { exception ->
                    _operationState.value = OperationState.Error(
                        exception.message ?: "Failed to create rule"
                    )
                }
        }
    }

    fun updateRule(rule: Rule) {
        viewModelScope.launch {
            _operationState.value = OperationState.Loading
            ruleRepository.updateRule(rule)
                .onSuccess {
                    _operationState.value = OperationState.Success("Rule updated successfully")
                }
                .onFailure { exception ->
                    _operationState.value = OperationState.Error(
                        exception.message ?: "Failed to update rule"
                    )
                }
        }
    }

    fun deleteRule(ruleId: String) {
        viewModelScope.launch {
            _operationState.value = OperationState.Loading
            ruleRepository.deleteRule(ruleId)
                .onSuccess {
                    _operationState.value = OperationState.Success("Rule deleted successfully")
                }
                .onFailure { exception ->
                    _operationState.value = OperationState.Error(
                        exception.message ?: "Failed to delete rule"
                    )
                }
        }
    }

    fun resetOperationState() {
        _operationState.value = OperationState.Idle
    }
}

data class AlertStats(
    val type: RuleType,
    val count: Int,
    val percentage: Float
)

sealed class OperationState {
    object Idle : OperationState()
    object Loading : OperationState()
    data class Success(val message: String) : OperationState()
    data class Error(val message: String) : OperationState()
}

