package pt.isec.a2022143267.safetysec.view.alert

import android.Manifest
import androidx.camera.core.CameraSelector
import androidx.camera.video.*
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import pt.isec.a2022143267.safetysec.R
import pt.isec.a2022143267.safetysec.viewmodel.AlertOperationState
import pt.isec.a2022143267.safetysec.viewmodel.AlertViewModel
import pt.isec.a2022143267.safetysec.viewmodel.AuthViewModel
import java.io.File

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AlertScreen(
    navController: NavController,
    alertId: String,
    authViewModel: AuthViewModel,
    alertViewModel: AlertViewModel = viewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val currentUser by authViewModel.currentUser.collectAsState()
    val countdown by alertViewModel.countdown.collectAsState()
    val alertState by alertViewModel.alertState.collectAsState()
    val isRecording by alertViewModel.isRecordingVideo.collectAsState()

    var cancelCode by remember { mutableStateOf("") }
    var showCancelDialog by remember { mutableStateOf(false) }
    var previewView: PreviewView? by remember { mutableStateOf(null) }

    // Initialize video recording helper
    LaunchedEffect(Unit) {
        alertViewModel.initializeVideoRecording(context, lifecycleOwner)
    }

    // Permission handling
    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
    )

    LaunchedEffect(Unit) {
        if (!permissionsState.allPermissionsGranted) {
            permissionsState.launchMultiplePermissionRequest()
        }
    }

    // Auto-start recording when alert becomes active
    LaunchedEffect(alertState) {
        if (alertState is AlertOperationState.Active &&
            permissionsState.allPermissionsGranted &&
            previewView != null) {
            alertViewModel.startVideoRecording(previewView!!)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.alert)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            when (alertState) {
                is AlertOperationState.Countdown -> {
                    CountdownView(
                        countdown = countdown,
                        onCancel = { showCancelDialog = true }
                    )
                }

                is AlertOperationState.Active -> {
                    if (isRecording && permissionsState.allPermissionsGranted) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = stringResource(R.string.video_recording),
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.error
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            CircularProgressIndicator(
                                modifier = Modifier.size(64.dp),
                                color = MaterialTheme.colorScheme.error
                            )

                            Spacer(modifier = Modifier.height(32.dp))

                            // Camera preview
                            AndroidView(
                                factory = { ctx ->
                                    PreviewView(ctx).also { previewView = it }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp)
                            )
                        }
                    } else {
                        Text(
                            text = stringResource(R.string.alert_sent),
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                }

                is AlertOperationState.Cancelled -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.alert_cancelled),
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        Button(onClick = { navController.navigateUp() }) {
                            Text(stringResource(R.string.close))
                        }
                    }
                }

                is AlertOperationState.VideoRecorded -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.alert_sent),
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = stringResource(R.string.video_uploaded))
                        Spacer(modifier = Modifier.height(32.dp))
                        Button(onClick = { navController.navigateUp() }) {
                            Text(stringResource(R.string.close))
                        }
                    }
                }

                is AlertOperationState.Error -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = (alertState as AlertOperationState.Error).message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        Button(onClick = { navController.navigateUp() }) {
                            Text(stringResource(R.string.close))
                        }
                    }
                }

                else -> {
                    CircularProgressIndicator()
                }
            }
        }
    }

    // Cancel Dialog
    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text(stringResource(R.string.cancel_alert)) },
            text = {
                Column {
                    Text(stringResource(R.string.enter_cancel_code))
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = cancelCode,
                        onValueChange = { cancelCode = it },
                        label = { Text(stringResource(R.string.cancel_code)) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        alertViewModel.cancelAlert(
                            inputCode = cancelCode,
                            correctCode = currentUser?.cancelCode ?: "",
                            cancelledBy = currentUser?.id ?: ""
                        )
                        showCancelDialog = false
                        cancelCode = ""
                    }
                ) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text(stringResource(R.string.back))
                }
            }
        )
    }
}

@Composable
fun CountdownView(
    countdown: Int,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Warning,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = stringResource(R.string.alert_detected),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.countdown, countdown),
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onCancel,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text(
                text = stringResource(R.string.cancel_alert),
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}

