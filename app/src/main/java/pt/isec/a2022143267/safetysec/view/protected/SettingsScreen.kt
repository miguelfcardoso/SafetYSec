package pt.isec.a2022143267.safetysec.view.auth

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import pt.isec.a2022143267.safetysec.R
import pt.isec.a2022143267.safetysec.model.UserType
import pt.isec.a2022143267.safetysec.navigation.Screen
import pt.isec.a2022143267.safetysec.viewmodel.AuthState
import pt.isec.a2022143267.safetysec.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current
    val currentUser by authViewModel.currentUser.collectAsState()
    val authState by authViewModel.authState.collectAsState()

    // Estado local para o novo código de cancelamento
    var newCancelCode by remember { mutableStateOf(currentUser?.cancelCode ?: "") }

    LaunchedEffect(currentUser) {
        if (newCancelCode.isEmpty()) {
            newCancelCode = currentUser?.cancelCode ?: ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Cartão de Informações do Perfil
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Informações da Conta",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Nome: ${currentUser?.name ?: "N/A"}")
                    Text("Email: ${currentUser?.email ?: "N/A"}")
                    Text("Tipo de Utilizador: ${currentUser?.userType?.name ?: "N/A"}")
                }
            }

            // Secção de Segurança - Apenas para o Utilizador Protegido
            if (currentUser?.userType == UserType.PROTECTED) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Segurança do Alerta",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = newCancelCode,
                            onValueChange = {
                                // Garante que apenas números e no máximo 4 dígitos são inseridos
                                if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                                    newCancelCode = it
                                }
                            },
                            label = { Text("Código de Cancelamento (4 dígitos)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                authViewModel.updateCancelCode(newCancelCode)
                                Toast.makeText(context, "Código atualizado!", Toast.LENGTH_SHORT).show()
                            },
                            // O botão só ativa se tiver 4 números E for diferente do atual
                            enabled = newCancelCode.length == 4 && newCancelCode != currentUser?.cancelCode && authState !is AuthState.Loading,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (authState is AuthState.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Atualizar Código")
                            }
                        }

                        Text(
                            text = "Este código é usado para cancelar alertas falsos durante a contagem de 10 segundos.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Botão de Terminar Sessão
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
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Terminar Sessão")
            }
        }
    }
}