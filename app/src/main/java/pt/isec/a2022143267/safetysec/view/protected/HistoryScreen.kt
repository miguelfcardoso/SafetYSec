package pt.isec.a2022143267.safetysec.view.protected

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import pt.isec.a2022143267.safetysec.R
import pt.isec.a2022143267.safetysec.model.Alert
import pt.isec.a2022143267.safetysec.model.AlertStatus
import pt.isec.a2022143267.safetysec.model.RuleType
import pt.isec.a2022143267.safetysec.utils.DateTimeUtils
import pt.isec.a2022143267.safetysec.viewmodel.AuthViewModel
import pt.isec.a2022143267.safetysec.viewmodel.ProtectedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    protectedViewModel: ProtectedViewModel = viewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val alerts by protectedViewModel.alerts.collectAsState()
    val operationState by protectedViewModel.operationState.collectAsState()

    var selectedFilter by remember { mutableStateOf<RuleType?>(null) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var selectedAlert by remember { mutableStateOf<Alert?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var alertToDelete by remember { mutableStateOf<Alert?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    // Show operation feedback
    LaunchedEffect(operationState) {
        when (operationState) {
            is pt.isec.a2022143267.safetysec.viewmodel.OperationState.Success -> {
                snackbarHostState.showSnackbar(
                    (operationState as pt.isec.a2022143267.safetysec.viewmodel.OperationState.Success).message
                )
                protectedViewModel.resetOperationState()
            }
            is pt.isec.a2022143267.safetysec.viewmodel.OperationState.Error -> {
                snackbarHostState.showSnackbar(
                    (operationState as pt.isec.a2022143267.safetysec.viewmodel.OperationState.Error).message
                )
                protectedViewModel.resetOperationState()
            }
            else -> {}
        }
    }

    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            protectedViewModel.loadAlerts(user.id)
        }
    }

    val filteredAlerts = if (selectedFilter != null) {
        alerts.filter { it.alertType == selectedFilter }
    } else {
        alerts
    }.sortedByDescending { it.timestamp }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.history)) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.filter))
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (alerts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.no_alerts_in_history),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                item {
                    if (selectedFilter != null) {
                        FilterChip(
                            selected = true,
                            onClick = { selectedFilter = null },
                            label = {Text(text = stringResource(R.string.filter_var, selectedFilter?.name ?: stringResource(R.string.all))) },
                            trailingIcon = {
                                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.clear_filter))
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                items(filteredAlerts) { alert ->
                    AlertHistoryCard(
                        alert = alert,
                        onClick = {
                            selectedAlert = alert
                        },
                        onDelete = {
                            alertToDelete = alert
                            showDeleteDialog = true
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    // Filter Dialog
    if (showFilterDialog) {
        AlertDialog(
            onDismissRequest = { showFilterDialog = false },
            title = { Text(stringResource(R.string.filter_by_type)) },
            text = {
                Column {
                    RuleType.values().forEach { type ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedFilter = type
                                    showFilterDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                getIconForRuleType(type),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(getTranslatedRuleName(type))
                        }
                    }

                    HorizontalDivider()

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedFilter = null
                                showFilterDialog = false
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(stringResource(R.string.show_all))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFilterDialog = false }) {
                    Text(stringResource(R.string.close))
                }
            }
        )
    }

    // Alert Details Dialog
    selectedAlert?.let { alert ->
        AlertDialog(
            onDismissRequest = { selectedAlert = null },
            icon = {
                Icon(
                    getIconForRuleType(alert.alertType),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = when (alert.status) {
                        AlertStatus.ACTIVE -> MaterialTheme.colorScheme.error
                        AlertStatus.CANCELLED -> MaterialTheme.colorScheme.outline
                        AlertStatus.RESOLVED -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
            },
            title = {
                Text(text = getTranslatedRuleName(alert.alertType))
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Timestamp
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.date_time),
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.width(120.dp)
                        )
                        Text(
                            text = DateTimeUtils.formatDateTime(alert.timestamp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    // Status
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.status_dp),
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.width(120.dp)
                        )
                        Text(
                            text = getTranslatedStatus(alert.status),
                            style = MaterialTheme.typography.bodyMedium,
                            color = when (alert.status) {
                                AlertStatus.ACTIVE -> MaterialTheme.colorScheme.error
                                AlertStatus.CANCELLED -> MaterialTheme.colorScheme.outline
                                AlertStatus.RESOLVED -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }

                    // Location
                    alert.location?.let { location ->
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Text(
                            text = stringResource(R.string.location_dp),
                            style = MaterialTheme.typography.labelMedium
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.latitude_dp),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.width(120.dp)
                            )
                            Text(
                                text = "${location.latitude}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.longitude_dp),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.width(120.dp)
                            )
                            Text(
                                text = "${location.longitude}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    // Additional data
                    if (alert.additionalData.isNotEmpty()) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Text(
                            text = stringResource(R.string.additional_information),
                            style = MaterialTheme.typography.labelMedium
                        )
                        alert.additionalData.forEach { (key, value) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = "$key:",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.width(120.dp)
                                )
                                Text(
                                    text = value,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }

                    // Video URL
                    if (alert.videoUrl.isNotEmpty()) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.video_recording_available),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Cancelled info
                    if (alert.status == AlertStatus.CANCELLED && alert.cancelledBy.isNotEmpty()) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Text(
                            text = stringResource(R.string.cancelled_by_user),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        alert.cancelledAt?.let {
                            Text(
                                text = stringResource(R.string.at, DateTimeUtils.formatDateTime(it)),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Row {
                    OutlinedButton(
                        onClick = {
                            alertToDelete = alert
                            selectedAlert = null
                            showDeleteDialog = true
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.delete))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { selectedAlert = null }) {
                        Text(stringResource(R.string.close))
                    }
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog && alertToDelete != null) {
        val isActiveAlert = alertToDelete?.status == AlertStatus.ACTIVE
        val isOldTestAlert = alertToDelete?.monitorId?.isEmpty() == true

        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                alertToDelete = null
            },
            icon = {
                Icon(
                    if (isActiveAlert) Icons.Default.Warning else Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(if (isActiveAlert) stringResource(R.string.delete_active_alert) else stringResource(R.string.delete_alert))
            },
            text = {
                Column {
                    if (isOldTestAlert) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.old_test_alert),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    if (isActiveAlert) {
                        Text(
                            text = stringResource(R.string.this_alert_has_active_status),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.delete_alert_permanently),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Text(
                        text = stringResource(R.string.delete_alert_confirmation),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        alertToDelete?.let { alert ->
                            protectedViewModel.deleteAlert(alert.id)
                        }
                        showDeleteDialog = false
                        alertToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(if (isActiveAlert) stringResource(R.string.delete_anyway) else stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        alertToDelete = null
                    }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun AlertHistoryCard(
    alert: Alert,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = when (alert.status) {
                AlertStatus.ACTIVE -> MaterialTheme.colorScheme.errorContainer
                AlertStatus.CANCELLED -> MaterialTheme.colorScheme.surfaceVariant
                AlertStatus.RESOLVED -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                getIconForRuleType(alert.alertType),
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = when (alert.status) {
                    AlertStatus.ACTIVE -> MaterialTheme.colorScheme.error
                    AlertStatus.CANCELLED -> MaterialTheme.colorScheme.outline
                    AlertStatus.RESOLVED -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = getTranslatedRuleName(alert.alertType),
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = DateTimeUtils.formatDateTime(alert.timestamp),
                    style = MaterialTheme.typography.bodySmall
                )

                Text(
                    text = stringResource(R.string.status_var, getTranslatedStatus(alert.status)),
                    style = MaterialTheme.typography.bodySmall,
                    color = when (alert.status) {
                        AlertStatus.ACTIVE -> MaterialTheme.colorScheme.error
                        AlertStatus.CANCELLED -> MaterialTheme.colorScheme.outline
                        AlertStatus.RESOLVED -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )

                // Show additional data if available
                if (alert.additionalData.isNotEmpty()) {
                    alert.additionalData.forEach { (key, value) ->
                        Text(
                            text = "$key: $value",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            IconButton(
                onClick = onDelete,
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete_alert)
                )
            }
        }
    }
}

@Composable
fun getTranslatedStatus(status: AlertStatus): String {
    return when (status) {
        AlertStatus.ACTIVE -> stringResource(R.string.active)
        AlertStatus.RESOLVED -> stringResource(R.string.resolved)
        AlertStatus.CANCELLED -> stringResource(R.string.cancelled)
        else -> status.name
    }
}

private fun getIconForRuleType(type: RuleType) = when (type) {
    RuleType.FALL_DETECTION -> Icons.Default.Warning
    RuleType.ACCIDENT -> Icons.Default.Warning
    RuleType.GEOFENCING -> Icons.Default.LocationOn
    RuleType.SPEED_CONTROL -> Icons.Default.Place
    RuleType.INACTIVITY -> Icons.Default.Person
    RuleType.PANIC_BUTTON -> Icons.Default.Notifications
}

