package pt.isec.a2022143267.safetysec.view.monitor

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Brush
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import pt.isec.a2022143267.safetysec.ui.theme.GradientEnd
import pt.isec.a2022143267.safetysec.ui.theme.GradientStart
import pt.isec.a2022143267.safetysec.ui.theme.isLandscape
import pt.isec.a2022143267.safetysec.viewmodel.AuthViewModel
import pt.isec.a2022143267.safetysec.viewmodel.MonitorViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonitorAlertDetailScreen(
    navController: NavController,
    alertId: String,
    authViewModel: AuthViewModel,
    monitorViewModel: MonitorViewModel = viewModel()
) {
    val context = LocalContext.current
    var alert by remember { mutableStateOf<Alert?>(null) }
    var protectedUserName by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Load alert details
    LaunchedEffect(alertId) {
        try {
            isLoading = true
            monitorViewModel.getAlertById(alertId)
                .onSuccess { loadedAlert ->
                    alert = loadedAlert
                    // Load protected user name
                    monitorViewModel.getUserById(loadedAlert.protectedId)
                        .onSuccess { user ->
                            protectedUserName = user.name
                        }
                    isLoading = false
                }
                .onFailure { exception ->
                    error = exception.message
                    isLoading = false
                }
        } catch (e: Exception) {
            error = e.message
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.alert_details)) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                error != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.error),
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = error ?: "Unknown error",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        Button(onClick = { navController.navigateUp() }) {
                            Text(stringResource(R.string.back))
                        }
                    }
                }
                alert != null -> {
                    AlertDetailsContent(
                        alert = alert!!,
                        protectedUserName = protectedUserName,
                        snackbarHostState = snackbarHostState,
                        onMarkResolved = {
                            monitorViewModel.updateAlertStatus(alertId, AlertStatus.RESOLVED)
                            navController.navigateUp()
                        },
                        onOpenMap = { lat, lon ->
                            try {
                                val uri = Uri.parse("geo:$lat,$lon?q=$lat,$lon")
                                val intent = Intent(Intent.ACTION_VIEW, uri)
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // Handle error silently or show a message
                            }
                        },
                        onPlayVideo = { videoUrl ->
                            try {
                                if (videoUrl.isNotEmpty() && videoUrl.startsWith("http")) {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl))
                                    intent.setDataAndType(Uri.parse(videoUrl), "video/*")
                                    context.startActivity(intent)
                                }
                            } catch (e: Exception) {
                                // Handle error silently - video player not available
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AlertDetailsContent(
    alert: Alert,
    protectedUserName: String,
    snackbarHostState: SnackbarHostState,
    onMarkResolved: () -> Unit,
    onOpenMap: (Double, Double) -> Unit,
    onPlayVideo: (String) -> Unit
) {
    val isLandscape = isLandscape()

    if (isLandscape) {
        // Landscape layout - Two columns
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Left column
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Alert Type Header
                item { AlertTypeHeader(alert) }
                // Protected User Info
                item { ProtectedUserCard(protectedUserName) }
                // Date and Time
                item { DateTimeCard(alert) }
            }

            // Right column
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Location
                if (alert.location != null) {
                    item { LocationCard(alert, onOpenMap) }
                }
                // Video Section
                item { VideoSection(alert, onPlayVideo) }
                // Mark as Resolved
                if (alert.status == AlertStatus.ACTIVE) {
                    item { ResolveButton(onMarkResolved) }
                }
            }
        }
    } else {
        // Portrait layout - Single column
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Alert Type Header
            item { AlertTypeHeader(alert) }

            // Protected User Info
            item { ProtectedUserCard(protectedUserName) }

            // Date and Time
            item { DateTimeCard(alert) }

            // Location
            if (alert.location != null) {
                item { LocationCard(alert, onOpenMap) }
            }

            // Video Section
            item { VideoSection(alert, onPlayVideo) }

            // Mark as Resolved
            if (alert.status == AlertStatus.ACTIVE) {
                item { ResolveButton(onMarkResolved) }
            }

            // Bottom spacing
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

// Extracted composable components for reuse in landscape/portrait modes

@Composable
fun AlertTypeHeader(alert: Alert) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = when (alert.status) {
                        AlertStatus.ACTIVE -> Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.error,
                                MaterialTheme.colorScheme.errorContainer
                            )
                        )
                        AlertStatus.RESOLVED -> Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.tertiary,
                                MaterialTheme.colorScheme.tertiaryContainer
                            )
                        )
                        else -> Brush.horizontalGradient(
                            colors = listOf(GradientStart, GradientEnd)
                        )
                    }
                )
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    modifier = Modifier.size(80.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
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
                            modifier = Modifier.size(48.dp),
                            tint = when (alert.status) {
                                AlertStatus.ACTIVE -> MaterialTheme.colorScheme.error
                                AlertStatus.RESOLVED -> MaterialTheme.colorScheme.tertiary
                                else -> MaterialTheme.colorScheme.primary
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = getTranslatedRuleType(alert.alertType),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.surface
                )
                Spacer(modifier = Modifier.height(8.dp))
                StatusChip(status = alert.status)
            }
        }
    }
}


@Composable
fun ProtectedUserCard(protectedUserName: String) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = stringResource(R.string.protected_user_full),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = protectedUserName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun DateTimeCard(alert: Alert) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = stringResource(R.string.date_time),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatTimestamp(alert.timestamp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun LocationCard(alert: Alert, onOpenMap: (Double, Double) -> Unit) {
    alert.location?.let { location ->
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(28.dp),
                                tint = MaterialTheme.colorScheme.onTertiary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.location),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "%.6f, %.6f".format(location.latitude, location.longitude),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                FilledTonalButton(
                    onClick = { onOpenMap(location.latitude, location.longitude) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary
                    )
                ) {
                    Icon(Icons.Default.Place, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("View on Map")
                }
            }
        }
    }
}

@Composable
fun VideoSection(alert: Alert, onPlayVideo: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.video_recording),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (alert.videoUrl.isNotEmpty()) "Video available" else "Video not available",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                }
            }

            if (alert.videoUrl.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { onPlayVideo(alert.videoUrl) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.view_video))
                }
            } else {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Video recording not available (Firebase Storage required)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
fun ResolveButton(onMarkResolved: () -> Unit) {
    Button(
        onClick = onMarkResolved,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Icon(Icons.Default.CheckCircle, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Mark as Resolved")
    }
}

@Composable
fun StatusChip(status: AlertStatus) {
    val (text, color) = when (status) {
        AlertStatus.PENDING -> Pair(
            "Pending",
            MaterialTheme.colorScheme.primary
        )
        AlertStatus.ACTIVE -> Pair(
            stringResource(R.string.active),
            MaterialTheme.colorScheme.error
        )
        AlertStatus.CANCELLED -> Pair(
            stringResource(R.string.cancelled),
            MaterialTheme.colorScheme.outline
        )
        AlertStatus.RESOLVED -> Pair(
            stringResource(R.string.resolved),
            MaterialTheme.colorScheme.tertiary
        )
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.2f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            color = color
        )
    }
}

private fun formatTimestamp(timestamp: Timestamp): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
    return sdf.format(timestamp.toDate())
}

