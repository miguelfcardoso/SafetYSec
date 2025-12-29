package pt.isec.a2022143267.safetysec.view.protected

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
import pt.isec.a2022143267.safetysec.view.components.PanicButton
import pt.isec.a2022143267.safetysec.viewmodel.AuthViewModel
import pt.isec.a2022143267.safetysec.viewmodel.ProtectedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProtectedDashboardScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    protectedViewModel: ProtectedViewModel = viewModel(),
    alertViewModel: pt.isec.a2022143267.safetysec.viewmodel.AlertViewModel = viewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val monitors by protectedViewModel.monitors.collectAsState()
    val pendingRelations by protectedViewModel.pendingRelations.collectAsState()
    val rules by protectedViewModel.rules.collectAsState()
    val generatedOTP by protectedViewModel.generatedOTP.collectAsState()
    val alertState by alertViewModel.alertState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    var showOTPDialog by remember { mutableStateOf(false) }

    // Show alert feedback
    LaunchedEffect(alertState) {
        when (alertState) {
            is pt.isec.a2022143267.safetysec.viewmodel.AlertOperationState.Countdown -> {
                snackbarHostState.showSnackbar(
                    message = "Panic alert sent! You have 10 seconds to cancel.",
                    duration = SnackbarDuration.Short
                )
            }
            is pt.isec.a2022143267.safetysec.viewmodel.AlertOperationState.Error -> {
                snackbarHostState.showSnackbar(
                    message = (alertState as pt.isec.a2022143267.safetysec.viewmodel.AlertOperationState.Error).message,
                    duration = SnackbarDuration.Long
                )
            }
            else -> {}
        }
    }

    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            protectedViewModel.loadMonitors(user.id)
            protectedViewModel.loadRules(user.id)
            protectedViewModel.loadPendingRelations(user.id)
            protectedViewModel.loadTimeWindows(user.id)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.my_profile)) },
                actions = {
                    IconButton(onClick = { showOTPDialog = true }) {
                        Icon(Icons.Default.Lock, contentDescription = stringResource(R.string.generate_otp))
                    }
                    IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                        Icon(Icons.Default.Settings, contentDescription = "Definições")
                    }
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
            PanicButton(
                onClick = {
                    currentUser?.let { user ->
                        alertViewModel.createPanicAlert(user.id, user)
                    }
                }
            )
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
                    text = "Welcome, ${currentUser?.name ?: "User"}",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Cancellation Code Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.cancel_code),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = currentUser?.cancelCode ?: "----",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Use this code to cancel alerts",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Generate OTP Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.generate_otp),
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Share with monitors to connect",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Button(
                                onClick = { showOTPDialog = true }
                            ) {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Generate")
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Quick Access Cards
            item {
                Text(
                    text = "Quick Access",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickAccessCard(
                        title = "Time Windows",
                        icon = Icons.Default.DateRange,
                        onClick = { navController.navigate(Screen.ProtectedTimeWindows.route) },
                        modifier = Modifier.weight(1f)
                    )
                    QuickAccessCard(
                        title = "My Monitors",
                        icon = Icons.Default.Person,
                        onClick = { navController.navigate(Screen.ProtectedMonitors.route) },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickAccessCard(
                        title = "Active Rules",
                        icon = Icons.Default.Settings,
                        onClick = { navController.navigate(Screen.ProtectedRules.route) },
                        modifier = Modifier.weight(1f)
                    )
                    QuickAccessCard(
                        title = "History",
                        icon = Icons.Default.Info,
                        onClick = { navController.navigate(Screen.ProtectedHistory.route) },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Pending Authorizations
            if (pendingRelations.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.authorizations),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(pendingRelations) { relation ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Monitor Request",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Monitor ID: ${relation.monitorId}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = { protectedViewModel.rejectRelation(relation.id) }
                                ) {
                                    Text(stringResource(R.string.reject))
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = { protectedViewModel.approveRelation(relation.id) }
                                ) {
                                    Text(stringResource(R.string.approve))
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Monitors section
            item {
                Text(
                    text = "My Monitors",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            if (monitors.isEmpty()) {
                item {
                    Text(
                        text = "No monitors assigned",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                items(monitors) { monitor ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
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
                                    text = monitor.name,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = monitor.email,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Active Rules section
            item {
                Text(
                    text = stringResource(R.string.monitoring_rules),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            if (rules.isEmpty()) {
                item {
                    Text(
                        text = "No rules configured",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                items(rules.filter { it.isEnabled }) { rule ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                when (rule.ruleType) {
                                    pt.isec.a2022143267.safetysec.model.RuleType.FALL_DETECTION -> Icons.Default.Warning
                                    pt.isec.a2022143267.safetysec.model.RuleType.ACCIDENT -> Icons.Default.Warning
                                    pt.isec.a2022143267.safetysec.model.RuleType.GEOFENCING -> Icons.Default.LocationOn
                                    pt.isec.a2022143267.safetysec.model.RuleType.SPEED_CONTROL -> Icons.Default.Place
                                    pt.isec.a2022143267.safetysec.model.RuleType.INACTIVITY -> Icons.Default.Person
                                    pt.isec.a2022143267.safetysec.model.RuleType.PANIC_BUTTON -> Icons.Default.Notifications
                                },
                                contentDescription = null,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = rule.name,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = rule.ruleType.name,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Text(
                                text = stringResource(R.string.active),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }

    // OTP Dialog
    if (showOTPDialog) {
        AlertDialog(
            onDismissRequest = {
                showOTPDialog = false
                protectedViewModel.clearOTP()
            },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.one_time_password))
                }
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (generatedOTP != null) {
                        Text(
                            text = "Your OTP code is:",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Text(
                                text = generatedOTP ?: "",
                                style = MaterialTheme.typography.displayMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "📱 Share this code with your monitor",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "⚠️ This code expires after use",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    } else {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Generate a one-time password to share with your monitor",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "This code will allow a monitor to send you connection request",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            },
            confirmButton = {
                if (generatedOTP == null) {
                    Button(onClick = {
                        currentUser?.let { user ->
                            protectedViewModel.generateOTP(user.id)
                        }
                    }) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.generate_otp))
                    }
                } else {
                    Button(onClick = {
                        showOTPDialog = false
                        protectedViewModel.clearOTP()
                    }) {
                        Text(stringResource(R.string.close))
                    }
                }
            },
            dismissButton = {
                if (generatedOTP == null) {
                    TextButton(onClick = { showOTPDialog = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            }
        )
    }
}

@Composable
fun QuickAccessCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
