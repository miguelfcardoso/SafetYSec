package pt.isec.a2022143267.safetysec.view.monitor

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
import pt.isec.a2022143267.safetysec.model.Rule
import pt.isec.a2022143267.safetysec.model.RuleParameters
import pt.isec.a2022143267.safetysec.model.RuleType
import pt.isec.a2022143267.safetysec.model.User
import pt.isec.a2022143267.safetysec.viewmodel.MonitorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProtectedDetailsScreen(
    navController: NavController,
    protectedUser: User,
    monitorViewModel: MonitorViewModel = viewModel(),
    authViewModel: pt.isec.a2022143267.safetysec.viewmodel.AuthViewModel = viewModel()
) {
    val rules by monitorViewModel.rules.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val operationState by monitorViewModel.operationState.collectAsState()


    var showCreateRuleDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Debug: Log when rules change
    LaunchedEffect(rules) {
        android.util.Log.d("ProtectedDetails", "Rules changed: ${rules.size} total, ${rules.count { it.isEnabled }} enabled")
    }

    // Show feedback messages
    LaunchedEffect(operationState) {
        when (operationState) {
            is pt.isec.a2022143267.safetysec.viewmodel.OperationState.Success -> {
                snackbarHostState.showSnackbar(
                    message = (operationState as pt.isec.a2022143267.safetysec.viewmodel.OperationState.Success).message,
                    duration = SnackbarDuration.Short
                )
                monitorViewModel.resetOperationState()
            }
            is pt.isec.a2022143267.safetysec.viewmodel.OperationState.Error -> {
                snackbarHostState.showSnackbar(
                    message = (operationState as pt.isec.a2022143267.safetysec.viewmodel.OperationState.Error).message,
                    duration = SnackbarDuration.Long
                )
                monitorViewModel.resetOperationState()
            }
            else -> {}
        }
    }

    LaunchedEffect(protectedUser.id) {
        monitorViewModel.loadRulesForProtected(protectedUser.id)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${protectedUser.name} - Rules") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateRuleDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Rule")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Protected User Info
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = protectedUser.name,
                                    style = MaterialTheme.typography.headlineSmall
                                )
                                Text(
                                    text = protectedUser.email,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Statistics
            item {
                val totalCount = rules.size
                val activeCount = rules.count { it.isEnabled }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    InfoCard(
                        title = "Total Rules",
                        value = totalCount.toString(),
                        modifier = Modifier.weight(1f)
                    )
                    InfoCard(
                        title = "Active Rules",
                        value = activeCount.toString(),
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Rules Section
            item {
                Text(
                    text = stringResource(R.string.monitoring_rules),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            if (rules.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No rules configured",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Create your first monitoring rule",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(
                    items = rules,
                    key = { rule -> "${rule.id}_${rule.isEnabled}_${rule.parameters}" }
                ) { rule ->
                    RuleCard(
                        rule = rule,
                        onToggle = { enabled ->
                            monitorViewModel.toggleRule(rule.id, enabled)
                        },
                        onDelete = {
                            monitorViewModel.deleteRule(rule.id)
                        },
                        onEditParameters = { newParameters ->
                            val updatedRule = rule.copy(parameters = newParameters)
                            monitorViewModel.updateRule(updatedRule)
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    // Create Rule Dialog
    if (showCreateRuleDialog) {
        CreateRuleDialog(
            protectedUser = protectedUser,
            monitorId = currentUser?.id ?: "",
            monitorViewModel = monitorViewModel,
            onDismiss = { showCreateRuleDialog = false },
            onRuleCreated = {
                showCreateRuleDialog = false
            }
        )
    }
}

@Composable
fun RuleCard(
    rule: Rule,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onEditParameters: (RuleParameters) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    // Local state for immediate UI response
    var isEnabledLocal by remember(rule.id, rule.isEnabled) { mutableStateOf(rule.isEnabled) }

    Card(
        modifier = Modifier.fillMaxWidth()
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
                    getIconForRuleType(rule.ruleType),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = if (isEnabledLocal)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = rule.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = rule.ruleType.name.replace("_", " "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (rule.description.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = rule.description,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Switch(
                    checked = isEnabledLocal,
                    onCheckedChange = { newValue ->
                        isEnabledLocal = newValue  // Update local state immediately
                        onToggle(newValue)          // Then call callback
                    }
                )
            }

            // Show parameters
            if (rule.parameters.maxSpeed > 0 ||
                rule.parameters.radius > 0 ||
                rule.parameters.inactivityMinutes > 0) {

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                Column {
                    when (rule.ruleType) {
                        RuleType.SPEED_CONTROL -> {
                            ParameterItem(
                                label = "Max Speed",
                                value = "${rule.parameters.maxSpeed} km/h"
                            )
                        }
                        RuleType.GEOFENCING -> {
                            ParameterItem(
                                label = "Radius",
                                value = "${rule.parameters.radius} meters"
                            )
                            rule.parameters.geoPoint?.let {
                                ParameterItem(
                                    label = "Location",
                                    value = "Lat: ${it.latitude}, Lng: ${it.longitude}"
                                )
                            }
                        }
                        RuleType.INACTIVITY -> {
                            ParameterItem(
                                label = "Inactivity Duration",
                                value = "${rule.parameters.inactivityMinutes} minutes"
                            )
                        }
                        else -> {}
                    }
                }
            }

            // Edit and Delete buttons
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Edit Parameters button (only for configurable rules)
                if (rule.ruleType == RuleType.SPEED_CONTROL ||
                    rule.ruleType == RuleType.GEOFENCING ||
                    rule.ruleType == RuleType.INACTIVITY) {
                    OutlinedButton(
                        onClick = { showEditDialog = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit Parameters")
                    }
                }

                // Delete button
                TextButton(
                    onClick = { showDeleteDialog = true },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = if (rule.ruleType == RuleType.SPEED_CONTROL ||
                                   rule.ruleType == RuleType.GEOFENCING ||
                                   rule.ruleType == RuleType.INACTIVITY) {
                        Modifier.weight(1f)
                    } else {
                        Modifier.fillMaxWidth()
                    }
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete Rule")
                }
            }
        }
    }

    // Edit Parameters Dialog
    if (showEditDialog) {
        EditRuleParametersDialog(
            rule = rule,
            onDismiss = { showEditDialog = false },
            onSave = { newParameters ->
                onEditParameters(newParameters)
                showEditDialog = false
            }
        )
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Rule") },
            text = { Text("Are you sure you want to delete this rule? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ParameterItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun CreateRuleDialog(
    protectedUser: User,
    monitorId: String,
    monitorViewModel: MonitorViewModel,
    onDismiss: () -> Unit,
    onRuleCreated: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Create Monitoring Rule")
        },
        text = {
            LazyColumn {
                item {
                    Text(
                        text = "Select rule type for ${protectedUser.name}:",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                items(RuleType.values().toList()) { ruleType ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        onClick = {
                            // Create rule with default parameters
                            val rule = Rule(
                                id = "",
                                name = ruleType.name.replace("_", " "),
                                description = getRuleDescription(ruleType),
                                ruleType = ruleType,
                                protectedId = protectedUser.id,
                                monitorId = monitorId,
                                isEnabled = true,
                                parameters = when (ruleType) {
                                    RuleType.SPEED_CONTROL -> pt.isec.a2022143267.safetysec.model.RuleParameters(maxSpeed = 80.0)
                                    RuleType.GEOFENCING -> pt.isec.a2022143267.safetysec.model.RuleParameters(
                                        radius = 500.0,
                                        geoPoint = null // User can set later
                                    )
                                    RuleType.INACTIVITY -> pt.isec.a2022143267.safetysec.model.RuleParameters(inactivityMinutes = 30)
                                    else -> pt.isec.a2022143267.safetysec.model.RuleParameters()
                                }
                            )
                            monitorViewModel.createRule(rule)
                            onRuleCreated()
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                getIconForRuleType(ruleType),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = ruleType.name.replace("_", " "),
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = getRuleDescription(ruleType),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun InfoCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private fun getIconForRuleType(ruleType: RuleType) = when (ruleType) {
    RuleType.FALL_DETECTION -> Icons.Default.Warning
    RuleType.ACCIDENT -> Icons.Default.Warning
    RuleType.GEOFENCING -> Icons.Default.LocationOn
    RuleType.SPEED_CONTROL -> Icons.Default.Place
    RuleType.INACTIVITY -> Icons.Default.Person
    RuleType.PANIC_BUTTON -> Icons.Default.Notifications
}

private fun getRuleDescription(ruleType: RuleType) = when (ruleType) {
    RuleType.FALL_DETECTION -> "Detect falls using accelerometer"
    RuleType.ACCIDENT -> "Detect sudden deceleration"
    RuleType.GEOFENCING -> "Alert when outside defined area"
    RuleType.SPEED_CONTROL -> "Alert on excessive speed"
    RuleType.INACTIVITY -> "Detect prolonged inactivity"
    RuleType.PANIC_BUTTON -> "Manual panic alert (always active)"
}

