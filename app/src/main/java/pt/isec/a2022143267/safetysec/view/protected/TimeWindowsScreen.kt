package pt.isec.a2022143267.safetysec.view.protected

import androidx.compose.animation.AnimatedVisibility
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
import pt.isec.a2022143267.safetysec.model.TimeWindow
import pt.isec.a2022143267.safetysec.viewmodel.AuthViewModel
import pt.isec.a2022143267.safetysec.viewmodel.ProtectedViewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeWindowsScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    protectedViewModel: ProtectedViewModel = viewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val timeWindows by protectedViewModel.timeWindows.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showCreateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            protectedViewModel.loadTimeWindows(user.id)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.time_windows)) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_time_window))
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (timeWindows.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.no_time_windows_configured),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.create_time_windows_note),
                        style = MaterialTheme.typography.bodyMedium,
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
                items(timeWindows) { window ->
                    TimeWindowCard(
                        timeWindow = window,
                        onDelete = {
                            protectedViewModel.deleteTimeWindow(window.id)
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateTimeWindowDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name, startHour, startMinute, endHour, endMinute, daysOfWeek ->
                currentUser?.let { user ->
                    val timeWindow = TimeWindow(
                        id = UUID.randomUUID().toString(),
                        protectedId = user.id,
                        name = name,
                        startHour = startHour,
                        startMinute = startMinute,
                        endHour = endHour,
                        endMinute = endMinute,
                        daysOfWeek = daysOfWeek,
                        isEnabled = true
                    )
                    protectedViewModel.createTimeWindow(timeWindow)
                    showCreateDialog = false
                }
            }
        )
    }
}

@Composable
fun TimeWindowCard(
    timeWindow: TimeWindow,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (timeWindow.isEnabled)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = timeWindow.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = String.format(
                            "%02d:%02d - %02d:%02d",
                            timeWindow.startHour,
                            timeWindow.startMinute,
                            timeWindow.endHour,
                            timeWindow.endMinute
                        ),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Icon(
                    if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Text(
                        text = stringResource(R.string.active_days),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val dayNames = listOf(stringResource(R.string.mon),
                            stringResource(R.string.tue),
                            stringResource(R.string.wed),
                            stringResource(R.string.thu),
                            stringResource(R.string.fri),
                            stringResource(R.string.sat),
                            stringResource(R.string.sun))
                        dayNames.forEachIndexed { index, dayName ->
                            val isActive = timeWindow.daysOfWeek.contains(index + 1)
                            FilterChip(
                                selected = isActive,
                                onClick = { },
                                label = { Text(dayName, style = MaterialTheme.typography.labelSmall) },
                                enabled = false
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = { showDeleteDialog = true },
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
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_time_window)) },
            text = { Text(stringResource(R.string.delete_time_window_confirmation, timeWindow.name)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTimeWindowDialog(
    onDismiss: () -> Unit,
    onCreate: (String, Int, Int, Int, Int, List<Int>) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var startHour by remember { mutableStateOf(9) }
    var startMinute by remember { mutableStateOf(0) }
    var endHour by remember { mutableStateOf(17) }
    var endMinute by remember { mutableStateOf(0) }
    var selectedDays by remember { mutableStateOf(listOf(1, 2, 3, 4, 5)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_time_window)) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.name)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.start_time),
                    style = MaterialTheme.typography.labelMedium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = startHour.toString(),
                        onValueChange = {
                            it.toIntOrNull()?.let { hour ->
                                if (hour in 0..23) startHour = hour
                            }
                        },
                        label = { Text(stringResource(R.string.hour)) },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = startMinute.toString(),
                        onValueChange = {
                            it.toIntOrNull()?.let { minute ->
                                if (minute in 0..59) startMinute = minute
                            }
                        },
                        label = { Text(stringResource(R.string.min)) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.end_time),
                    style = MaterialTheme.typography.labelMedium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = endHour.toString(),
                        onValueChange = {
                            it.toIntOrNull()?.let { hour ->
                                if (hour in 0..23) endHour = hour
                            }
                        },
                        label = { Text(stringResource(R.string.hour)) },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = endMinute.toString(),
                        onValueChange = {
                            it.toIntOrNull()?.let { minute ->
                                if (minute in 0..59) endMinute = minute
                            }
                        },
                        label = { Text(stringResource(R.string.min)) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.days_of_week),
                    style = MaterialTheme.typography.labelMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                val dayNames = listOf(stringResource(R.string.mon),
                    stringResource(R.string.tue),
                    stringResource(R.string.wed),
                    stringResource(R.string.thu),
                    stringResource(R.string.fri),
                    stringResource(R.string.sat),
                    stringResource(R.string.sun))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    dayNames.chunked(4).forEach { chunk ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            chunk.forEachIndexed { index, dayName ->
                                val globalIndex = dayNames.indexOf(dayName) + 1
                                val isSelected = selectedDays.contains(globalIndex)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedDays = if (isSelected) {
                                            selectedDays - globalIndex
                                        } else {
                                            selectedDays + globalIndex
                                        }
                                    },
                                    label = { Text(dayName) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            // Fill remaining space
                            repeat(4 - chunk.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && selectedDays.isNotEmpty()) {
                        onCreate(name, startHour, startMinute, endHour, endMinute, selectedDays)
                    }
                },
                enabled = name.isNotBlank() && selectedDays.isNotEmpty()
            ) {
                Text(stringResource(R.string.create))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

