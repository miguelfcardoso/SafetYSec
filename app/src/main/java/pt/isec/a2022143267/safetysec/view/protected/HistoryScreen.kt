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

    var selectedFilter by remember { mutableStateOf<RuleType?>(null) }
    var showFilterDialog by remember { mutableStateOf(false) }

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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Filter")
                    }
                }
            )
        }
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
                        text = "No alerts in history",
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
                            label = { Text("Filter: ${selectedFilter?.name}") },
                            trailingIcon = {
                                Icon(Icons.Default.Close, contentDescription = "Clear filter")
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                items(filteredAlerts) { alert ->
                    AlertHistoryCard(
                        alert = alert,
                        onClick = {
                            // Navigate to alert details
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
            title = { Text("Filter by type") },
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
                            Text(type.name)
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
                        Text("Show All")
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
}

@Composable
fun AlertHistoryCard(
    alert: Alert,
    onClick: () -> Unit
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
                    text = alert.alertType.name.replace("_", " "),
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = DateTimeUtils.formatDateTime(alert.timestamp),
                    style = MaterialTheme.typography.bodySmall
                )

                Text(
                    text = "Status: ${alert.status.name}",
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

            Icon(
                Icons.Default.KeyboardArrowRight,
                contentDescription = null
            )
        }
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

