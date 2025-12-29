package pt.isec.a2022143267.safetysec.view.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import pt.isec.a2022143267.safetysec.R
import pt.isec.a2022143267.safetysec.model.UserType
import pt.isec.a2022143267.safetysec.navigation.Screen
import pt.isec.a2022143267.safetysec.viewmodel.AuthState
import pt.isec.a2022143267.safetysec.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImprovedSettingsScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val authState by authViewModel.authState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Local states for form fields
    var nameText by remember { mutableStateOf(currentUser?.name ?: "") }
    var newCancelCode by remember { mutableStateOf(currentUser?.cancelCode ?: "") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    // Expanded states for cards
    var isPersonalInfoExpanded by remember { mutableStateOf(true) }
    var isPasswordExpanded by remember { mutableStateOf(false) }
    var isCancelCodeExpanded by remember { mutableStateOf(false) }

    // Update fields when user data loads
    LaunchedEffect(currentUser) {
        if (nameText.isEmpty()) nameText = currentUser?.name ?: ""
        if (newCancelCode.isEmpty()) newCancelCode = currentUser?.cancelCode ?: ""
    }

    // Show feedback messages
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Error -> {
                snackbarHostState.showSnackbar(
                    message = (authState as AuthState.Error).message,
                    duration = SnackbarDuration.Short
                )
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.settings))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // User Info Card at the top
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
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
                        modifier = Modifier.size(64.dp),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.secondary
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.onSecondary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = currentUser?.name ?: "Protected User",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = currentUser?.email ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        AssistChip(
                            onClick = { },
                            label = { Text("Protected") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.secondary,
                                labelColor = MaterialTheme.colorScheme.onSecondary,
                                leadingIconContentColor = MaterialTheme.colorScheme.onSecondary
                            )
                        )
                    }
                }
            }

            // Personal Information Card
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { isPersonalInfoExpanded = !isPersonalInfoExpanded }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Personal Information",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Icon(
                            if (isPersonalInfoExpanded) Icons.Default.KeyboardArrowUp
                            else Icons.Default.KeyboardArrowDown,
                            contentDescription = null
                        )
                    }

                    AnimatedVisibility(
                        visible = isPersonalInfoExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(modifier = Modifier.padding(top = 16.dp)) {
                            OutlinedTextField(
                                value = nameText,
                                onValueChange = { nameText = it },
                                label = { Text("Name") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                leadingIcon = {
                                    Icon(Icons.Default.Edit, contentDescription = null)
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    focusedLabelColor = MaterialTheme.colorScheme.primary
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    authViewModel.updateName(nameText)
                                },
                                enabled = nameText.isNotBlank() &&
                                         nameText != currentUser?.name &&
                                         authState !is AuthState.Loading,
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(16.dp)
                            ) {
                                if (authState is AuthState.Loading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text("Update Name")
                            }
                        }
                    }
                }
            }

            // Password Change Card
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { isPasswordExpanded = !isPasswordExpanded }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Change Password",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Icon(
                            if (isPasswordExpanded) Icons.Default.KeyboardArrowUp
                            else Icons.Default.KeyboardArrowDown,
                            contentDescription = null
                        )
                    }

                    AnimatedVisibility(
                        visible = isPasswordExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(modifier = Modifier.padding(top = 16.dp)) {
                            OutlinedTextField(
                                value = newPassword,
                                onValueChange = { newPassword = it },
                                label = { Text("New Password") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                visualTransformation = if (showPassword)
                                    VisualTransformation.None
                                else
                                    PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                leadingIcon = {
                                    Icon(Icons.Default.Lock, contentDescription = null)
                                },
                                trailingIcon = {
                                    IconButton(onClick = { showPassword = !showPassword }) {
                                        Icon(
                                            if (showPassword) Icons.Default.Close
                                            else Icons.Default.Check,
                                            contentDescription = null
                                        )
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    focusedLabelColor = MaterialTheme.colorScheme.primary
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                label = { Text("Confirm Password") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                visualTransformation = if (showConfirmPassword)
                                    VisualTransformation.None
                                else
                                    PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                leadingIcon = {
                                    Icon(Icons.Default.Lock, contentDescription = null)
                                },
                                trailingIcon = {
                                    IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                                        Icon(
                                            if (showConfirmPassword) Icons.Default.Close
                                            else Icons.Default.Check,
                                            contentDescription = null
                                        )
                                    }
                                },
                                isError = confirmPassword.isNotEmpty() && newPassword != confirmPassword,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    focusedLabelColor = MaterialTheme.colorScheme.primary
                                )
                            )

                            if (confirmPassword.isNotEmpty() && newPassword != confirmPassword) {
                                Text(
                                    text = "Passwords do not match",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    authViewModel.changePassword(newPassword, confirmPassword)
                                    newPassword = ""
                                    confirmPassword = ""
                                },
                                enabled = newPassword.isNotEmpty() &&
                                         newPassword == confirmPassword &&
                                         newPassword.length >= 6 &&
                                         authState !is AuthState.Loading,
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(16.dp)
                            ) {
                                if (authState is AuthState.Loading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text("Change Password")
                            }

                            if (newPassword.isNotEmpty() && newPassword.length < 6) {
                                Text(
                                    text = "Password must be at least 6 characters",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(start = 16.dp, top = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Cancellation Code Card (Protected users only)
            if (currentUser?.userType == UserType.PROTECTED) {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { isCancelCodeExpanded = !isCancelCodeExpanded }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    "Alert Cancellation PIN",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            Icon(
                                if (isCancelCodeExpanded) Icons.Default.KeyboardArrowUp
                                else Icons.Default.KeyboardArrowDown,
                                contentDescription = null
                            )
                        }

                        AnimatedVisibility(
                            visible = isCancelCodeExpanded,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column(modifier = Modifier.padding(top = 16.dp)) {
                                Text(
                                    text = "This PIN is used to cancel false alerts within 10 seconds",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                OutlinedTextField(
                                    value = newCancelCode,
                                    onValueChange = {
                                        if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                                            newCancelCode = it
                                        }
                                    },
                                    label = { Text("New PIN (4 digits)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    leadingIcon = {
                                        Icon(Icons.Default.Warning, contentDescription = null)
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        focusedLabelColor = MaterialTheme.colorScheme.primary
                                    )
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        authViewModel.updateCancelCode(newCancelCode)
                                    },
                                    enabled = newCancelCode.length == 4 &&
                                             newCancelCode != currentUser?.cancelCode &&
                                             authState !is AuthState.Loading,
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = PaddingValues(16.dp)
                                ) {
                                    if (authState is AuthState.Loading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.dp,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }
                                    Text("Update PIN")
                                }

                                if (newCancelCode.isNotEmpty() && newCancelCode.length < 4) {
                                    Text(
                                        text = "PIN must be exactly 4 digits",
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(start = 16.dp, top = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Logout Button
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                )
            ) {
                Button(
                    onClick = {
                        authViewModel.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Icon(
                        Icons.Default.ExitToApp,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Logout")
                }
            }
        }
    }
}

