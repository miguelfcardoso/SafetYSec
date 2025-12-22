package pt.isec.a2022143267.safetysec.view.monitor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
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
import pt.isec.a2022143267.safetysec.navigation.Screen
import pt.isec.a2022143267.safetysec.viewmodel.AuthViewModel
import pt.isec.a2022143267.safetysec.viewmodel.MonitorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonitorDashboardScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    monitorViewModel: MonitorViewModel = viewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val protectedUsers by monitorViewModel.protectedUsers.collectAsState()
    val activeAlerts by monitorViewModel.activeAlerts.collectAsState()
    val alerts by monitorViewModel.alerts.collectAsState()
    val operationState by monitorViewModel.operationState.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var otpInput by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }

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

    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            monitorViewModel.loadProtectedUsers(user.id)
            monitorViewModel.loadActiveAlerts(user.id)
            monitorViewModel.loadAlerts(user.id)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.dashboard)) },
                actions = {
                    IconButton(onClick = {
                        authViewModel.logout()
                        navController.navigate(pt.isec.a2022143267.safetysec.navigation.Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = stringResource(R.string.logout))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_protected))
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
            // Welcome section
            item {
                Text(
                    text = "Welcome, ${currentUser?.name ?: "Monitor"}",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Statistics cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        title = stringResource(R.string.protected_users),
                        value = protectedUsers.size.toString(),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = stringResource(R.string.active_alerts),
                        value = activeAlerts.size.toString(),
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Protected users section
            item {
                Text(
                    text = stringResource(R.string.protected_users),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            if (protectedUsers.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.no_protected_users),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                items(protectedUsers) { user ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        onClick = {
                            navController.navigate(
                                pt.isec.a2022143267.safetysec.navigation.Screen.MonitorProtectedDetails.createRoute(user.id)
                            )
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = user.name,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = user.email,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Icon(
                                Icons.Default.KeyboardArrowRight,
                                contentDescription = null
                            )
                        }
                    }
                }
            }

            // Recent alerts section
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.active_alerts),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            if (activeAlerts.isEmpty()) {
                item {
                    Text(
                        text = "No active alerts",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                items(activeAlerts) { alert ->
                    AlertCard(alert = alert, onClick = {
                        // Navigate to alert details
                    })
                }
            }
        }
    }

    // Add Protected User Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                otpInput = ""
            },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.add_protected))
                }
            },
            text = {
                Column {
                    Text(
                        text = stringResource(R.string.otp_hint),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = otpInput,
                        onValueChange = { otpInput = it },
                        label = { Text(stringResource(R.string.one_time_password)) },
                        placeholder = { Text("Enter 6-digit code") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = operationState !is pt.isec.a2022143267.safetysec.viewmodel.OperationState.Loading
                    )

                    if (operationState is pt.isec.a2022143267.safetysec.viewmodel.OperationState.Loading) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("Verifying OTP...")
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (otpInput.isNotEmpty()) {
                            currentUser?.let { user ->
                                monitorViewModel.addProtectedUserWithOTP(user.id, otpInput)
                            }
                        }
                    },
                    enabled = otpInput.isNotEmpty() &&
                             operationState !is pt.isec.a2022143267.safetysec.viewmodel.OperationState.Loading
                ) {
                    Text(stringResource(R.string.verify))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        otpInput = ""
                        showAddDialog = false
                    },
                    enabled = operationState !is pt.isec.a2022143267.safetysec.viewmodel.OperationState.Loading
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )

        // Auto-close dialog on success
        LaunchedEffect(operationState) {
            if (operationState is pt.isec.a2022143267.safetysec.viewmodel.OperationState.Success) {
                otpInput = ""
                showAddDialog = false
            }
        }
    }
}

@Composable
fun StatCard(
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
                style = MaterialTheme.typography.headlineLarge
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun AlertCard(
    alert: pt.isec.a2022143267.safetysec.model.Alert,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = alert.alertType.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = "Status: ${alert.status.name}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

