package pt.isec.a2022143267.safetysec.view.protected

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import pt.isec.a2022143267.safetysec.model.Rule
import pt.isec.a2022143267.safetysec.model.RuleType
import pt.isec.a2022143267.safetysec.viewmodel.AuthViewModel
import pt.isec.a2022143267.safetysec.viewmodel.ProtectedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProtectedRulesScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    protectedViewModel: ProtectedViewModel = viewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val rules by protectedViewModel.rules.collectAsState()
    val monitors by protectedViewModel.monitors.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedFilter by remember { mutableStateOf<String?>(null) } // Monitor ID filter

    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            protectedViewModel.loadRules(user.id)
            protectedViewModel.loadMonitors(user.id)
        }
    }

    val filteredRules = if (selectedFilter != null) {
        rules.filter { it.monitorId == selectedFilter }
    } else {
        rules
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.monitoring_rules)) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Filter by monitor
            if (monitors.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedFilter == null,
                            onClick = { selectedFilter = null },
                            label = { Text(stringResource(R.string.all)) }
                        )
                    }

                    items(monitors) { monitor ->
                        FilterChip(
                            selected = selectedFilter == monitor.id,
                            onClick = { selectedFilter = monitor.id },
                            label = { Text(monitor.name) }
                        )
                    }
                }
            }

            if (filteredRules.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
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
                            text = stringResource(R.string.no_rules_configured),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    items(filteredRules) { rule ->
                        RuleCard(
                            rule = rule,
                            monitorName = monitors.find { it.id == rule.monitorId }?.name ?: stringResource(R.string.unknown),
                            onToggleEnabled = { enabled ->
                                protectedViewModel.updateRuleStatus(rule.id, enabled)
                            },
                            onRevoke = {
                                protectedViewModel.revokeRule(rule.id)
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun RuleCard(
    rule: Rule,
    monitorName: String,
    onToggleEnabled: (Boolean) -> Unit,
    onRevoke: () -> Unit
) {
    var showRevokeDialog by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (rule.isEnabled)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    getRuleIcon(rule.ruleType),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = if (rule.isEnabled)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.outline
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = getTranslatedRuleName(rule.ruleType),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = stringResource(R.string.by_monitor, monitorName),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Switch(
                    checked = rule.isEnabled,
                    onCheckedChange = onToggleEnabled
                )
            }

            // Show parameters if available
            val hasParameters = when (rule.ruleType) {
                RuleType.GEOFENCING -> rule.parameters.geoPoint != null
                RuleType.SPEED_CONTROL -> true
                RuleType.INACTIVITY -> true
                else -> false
            }

            if (hasParameters) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.parameters),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Column {
                    when (rule.ruleType) {
                        RuleType.GEOFENCING -> {
                            rule.parameters.geoPoint?.let { geoPoint ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.location_dp),
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.width(120.dp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${geoPoint.latitude}, ${geoPoint.longitude}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.radius_dp),
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.width(120.dp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${rule.parameters.radius} m",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                        RuleType.SPEED_CONTROL -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.max_speed_dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.width(120.dp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${rule.parameters.maxSpeed} km/h",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        RuleType.INACTIVITY -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.inactivity_time_dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.width(120.dp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${rule.parameters.inactivityMinutes} min",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        else -> {}
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { showRevokeDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.revoke_rule))
            }
        }
    }

    if (showRevokeDialog) {
        AlertDialog(
            onDismissRequest = { showRevokeDialog = false },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text(stringResource(R.string.revoke_rule)) },
            text = {
                Text(
                    stringResource(
                        R.string.revoke_rule_confirmation,
                        getTranslatedRuleName(rule.ruleType)
                    ))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRevoke()
                        showRevokeDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.revoke))
                }
            },
            dismissButton = {
                TextButton(onClick = { showRevokeDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun getTranslatedRuleName(ruleType: RuleType): String {
    return when (ruleType) {
        RuleType.FALL_DETECTION -> stringResource(R.string.fall_detection)
        RuleType.GEOFENCING -> stringResource(R.string.geofencing)
        RuleType.SPEED_CONTROL -> stringResource(R.string.speed_control)
        RuleType.INACTIVITY -> stringResource(R.string.inactivity)
        RuleType.ACCIDENT -> stringResource(R.string.accident)
        RuleType.PANIC_BUTTON -> stringResource(R.string.panic_button)
    }
}

private fun getRuleIcon(ruleType: RuleType) = when (ruleType) {
    RuleType.FALL_DETECTION -> Icons.Default.Warning
    RuleType.ACCIDENT -> Icons.Default.Warning
    RuleType.GEOFENCING -> Icons.Default.LocationOn
    RuleType.SPEED_CONTROL -> Icons.Default.Place
    RuleType.INACTIVITY -> Icons.Default.Person
    RuleType.PANIC_BUTTON -> Icons.Default.Notifications
}

