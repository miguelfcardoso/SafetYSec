package pt.isec.a2022143267.safetysec.view.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import pt.isec.a2022143267.safetysec.navigation.Screen
import pt.isec.a2022143267.safetysec.viewmodel.AuthState
import pt.isec.a2022143267.safetysec.viewmodel.AuthViewModel

@Composable
fun MFAScreen(navController: NavController, authViewModel: AuthViewModel) {
    var code by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    val authState by authViewModel.authState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated && currentUser != null) {
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Verificação de Segurança", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Insira o código de 6 dígitos para continuar.",
            modifier = Modifier.padding(bottom = 24.dp)
        )

        OutlinedTextField(
            value = code,
            onValueChange = {
                if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                    code = it
                    isError = false
                }
            },
            label = { Text("Código MFA") },
            isError = isError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            supportingText = {
                if (isError) {
                    Text("Código incorreto. Tente novamente.", color = MaterialTheme.colorScheme.error)
                }
            }
        )

        Button(
            onClick = {
                if (code == "123456") {
                    isError = false
                    authViewModel.completeMFA()
                } else {
                    isError = true
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            enabled = code.length == 6
        ) {
            Text("Verificar")
        }
    }
}