package pt.isec.a2022143267.safetysec.view.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import pt.isec.a2022143267.safetysec.R
import pt.isec.a2022143267.safetysec.navigation.Screen
import pt.isec.a2022143267.safetysec.viewmodel.AuthState
import pt.isec.a2022143267.safetysec.viewmodel.AuthViewModel

@Composable
fun MFAScreen(navController: NavController, authViewModel: AuthViewModel) {
    var code by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val authState by authViewModel.authState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val otpCode by authViewModel.otpCode.collectAsState() // For development/testing

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> {
                if (currentUser != null) {
                    val destination = if (currentUser?.userType == pt.isec.a2022143267.safetysec.model.UserType.MONITOR) {
                        Screen.MonitorDashboard.route
                    } else {
                        Screen.ProtectedDashboard.route
                    }
                    navController.navigate(destination) {
                        popUpTo(Screen.MFA.route) { inclusive = true }
                    }
                }
            }
            is AuthState.Error -> {
                isError = true
                errorMessage = (authState as AuthState.Error).message
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            stringResource(R.string.security_check),
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            stringResource(R.string.enter_mfa_code),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Development mode: Show OTP (remove in production)
        if (otpCode.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Development Mode",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        "Your OTP: $otpCode",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Expires in 10 minutes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = code,
            onValueChange = {
                if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                    code = it
                    isError = false
                }
            },
            label = { Text(stringResource(R.string.mfa_code)) },
            isError = isError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            supportingText = {
                if (isError) {
                    Text(
                        errorMessage.ifEmpty { stringResource(R.string.wrong_mfa) },
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        )

        Button(
            onClick = {
                authViewModel.verifyMFACode(code)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            enabled = code.length == 6 && authState !is AuthState.Loading
        ) {
            if (authState is AuthState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(stringResource(R.string.check))
            }
        }

        TextButton(
            onClick = { navController.navigateUp() },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Cancel")
        }
    }
}