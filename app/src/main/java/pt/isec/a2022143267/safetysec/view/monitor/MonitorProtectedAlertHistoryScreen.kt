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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.Timestamp
import pt.isec.a2022143267.safetysec.R
import pt.isec.a2022143267.safetysec.model.Alert
import pt.isec.a2022143267.safetysec.model.AlertStatus
import pt.isec.a2022143267.safetysec.model.RuleType
import pt.isec.a2022143267.safetysec.viewmodel.AuthViewModel
import pt.isec.a2022143267.safetysec.viewmodel.MonitorViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonitorProtectedAlertHistoryScreen(
    navController: NavController,
    protectedId: String,
    authViewModel: AuthViewModel,
    monitorViewModel: MonitorViewModel = viewModel()
) {
    val alerts by monitorViewModel.alerts.collectAsState()
    var protectedUserName by remember { mutableStateOf("") }
    var filterType by remember { mutableStateOf<RuleType?>(null) }
    var showFilterDialog by remember { mutableStateOf(false) }

    // Load protected user name
    LaunchedEffect(protectedId) {
        monitorViewModel.loadAlertsForProtected(protectedId)
        monitorViewModel.getUserById(protectedId)
            .onSuccess { user ->
                protectedUserName = user.name
            }
    }

    val filteredAlerts = if (filterType != null) {
        alerts.filter { it.alertType == filterType }
    } else {
        alerts
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.alert_history))
                        if (protectedUserName.isNotEmpty()) {
                            Text(
                                text = protectedUserName,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = stringResource(R.string.filter),
                            tint = if (filterType != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Filter chip
            if (filterType != null) {
                FilterChip(
                    selected = true,
                    onClick = { filterType = null },
                    label = { Text(stringResource(R.string.filter_var, getTranslatedRuleType(filterType!!))) },
                    trailingIcon = {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = stringResource(R.string.clear_filter),
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            if (filteredAlerts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.no_alerts_in_history),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = filteredAlerts.sortedByDescending { it.timestamp.seconds },
                        key = { it.id }
                    ) { alert ->
                        AlertHistoryCard(
                            alert = alert,
                            onClick = {
                                navController.navigate(
                                    pt.isec.a2022143267.safetysec.navigation.Screen.MonitorAlertDetail.createRoute(alert.id)
                                )
                            }
                        )
                    }
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
                    TextButton(
                        onClick = {
                            filterType = null
                            showFilterDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.show_all),
                            modifier = Modifier.weight(1f)
                        )
                        if (filterType == null) {
                            Icon(Icons.Default.Check, contentDescription = null)
                        }
                    }

                    RuleType.entries.forEach { type ->
                        TextButton(
                            onClick = {
                                filterType = type
                                showFilterDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = getTranslatedRuleType(type),
                                modifier = Modifier.weight(1f)
                            )
                            if (filterType == type) {
                                Icon(Icons.Default.Check, contentDescription = null)
                            }
                        }
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
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = when (alert.status) {
                AlertStatus.ACTIVE -> MaterialTheme.colorScheme.errorContainer
                AlertStatus.RESOLVED -> MaterialTheme.colorScheme.tertiaryContainer
                AlertStatus.CANCELLED -> MaterialTheme.colorScheme.surfaceVariant
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
            // Icon
            Icon(
                when (alert.alertType) {
                    RuleType.FALL_DETECTION -> Icons.Default.Warning
                    RuleType.ACCIDENT -> Icons.Default.Warning
                    RuleType.PANIC_BUTTON -> Icons.Default.Warning
                    RuleType.GEOFENCING -> Icons.Default.LocationOn
                    RuleType.SPEED_CONTROL -> Icons.Default.Warning
                    RuleType.INACTIVITY -> Icons.Default.Person
                },
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = when (alert.status) {
                    AlertStatus.ACTIVE -> MaterialTheme.colorScheme.error
                    AlertStatus.RESOLVED -> MaterialTheme.colorScheme.tertiary
                    AlertStatus.CANCELLED -> MaterialTheme.colorScheme.outline
                    else -> MaterialTheme.colorScheme.primary
                }
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = getTranslatedRuleType(alert.alertType),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatTimestamp(alert.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Status badge
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = when (alert.status) {
                        AlertStatus.ACTIVE -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                        AlertStatus.RESOLVED -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                        AlertStatus.CANCELLED -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    }
                ) {
                    Text(
                        text = when (alert.status) {
                            AlertStatus.ACTIVE -> stringResource(R.string.active)
                            AlertStatus.RESOLVED -> stringResource(R.string.resolved)
                            AlertStatus.CANCELLED -> stringResource(R.string.cancelled)
                            AlertStatus.PENDING -> stringResource(R.string.pending)
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = when (alert.status) {
                            AlertStatus.ACTIVE -> MaterialTheme.colorScheme.error
                            AlertStatus.RESOLVED -> MaterialTheme.colorScheme.tertiary
                            AlertStatus.CANCELLED -> MaterialTheme.colorScheme.outline
                            else -> MaterialTheme.colorScheme.primary
                        }
                    )
                }

                // Show location if available
                if (alert.location != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${String.format("%.4f", alert.location.latitude)}, ${String.format("%.4f", alert.location.longitude)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Chevron
            Icon(
                Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatTimestamp(timestamp: Timestamp): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(timestamp.toDate())
}

