package pt.isec.a2022143267.safetysec.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import pt.isec.a2022143267.safetysec.model.UserType
import pt.isec.a2022143267.safetysec.view.auth.LoginScreen
import pt.isec.a2022143267.safetysec.view.auth.RegisterScreen
import pt.isec.a2022143267.safetysec.view.auth.ForgotPasswordScreen
import pt.isec.a2022143267.safetysec.view.monitor.MonitorDashboardScreen
import pt.isec.a2022143267.safetysec.view.monitor.MonitorSettingsScreen
import pt.isec.a2022143267.safetysec.view.protected.ProtectedDashboardScreen
import pt.isec.a2022143267.safetysec.view.protected.HistoryScreen
import pt.isec.a2022143267.safetysec.view.alert.AlertScreen
import pt.isec.a2022143267.safetysec.view.auth.MFAScreen
import pt.isec.a2022143267.safetysec.view.auth.SettingsScreen
import pt.isec.a2022143267.safetysec.viewmodel.AlertViewModel
import pt.isec.a2022143267.safetysec.viewmodel.AuthViewModel

/**
 * Main navigation graph for the app
 */
@Composable
fun AppNavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel = viewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()

    val alertViewModel: AlertViewModel = viewModel()

    val startDestination = when {
        currentUser == null -> Screen.Login.route
        currentUser?.userType == UserType.MONITOR -> Screen.MonitorDashboard.route
        else -> Screen.ProtectedDashboard.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Authentication screens
        composable(Screen.Login.route) {
            LoginScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        composable(Screen.MFA.route) {
            MFAScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        // Monitor screens
        composable(Screen.MonitorDashboard.route) {
            MonitorDashboardScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        composable(Screen.MonitorSettings.route) {
            MonitorSettingsScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        composable(
            route = Screen.MonitorProtectedDetails.route,
            arguments = listOf(navArgument("protectedId") { type = NavType.StringType })
        ) { backStackEntry ->
            val protectedId = backStackEntry.arguments?.getString("protectedId") ?: ""
            val monitorViewModel: pt.isec.a2022143267.safetysec.viewmodel.MonitorViewModel = viewModel()
            val currentMonitorUser by authViewModel.currentUser.collectAsState()

            // Load protected users for this monitor
            LaunchedEffect(currentMonitorUser) {
                currentMonitorUser?.let { user ->
                    monitorViewModel.loadProtectedUsers(user.id)
                }
            }

            // Get the protected user
            val protectedUsers by monitorViewModel.protectedUsers.collectAsState()
            val protectedUser = protectedUsers.find { it.id == protectedId }

            if (protectedUser != null) {
                pt.isec.a2022143267.safetysec.view.monitor.ProtectedDetailsScreen(
                    navController = navController,
                    protectedUser = protectedUser,
                    monitorViewModel = monitorViewModel
                )
            } else if (protectedUsers.isNotEmpty()) {
                // Protected user not found but list is loaded
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Protected user not found")
                }
            } else {
                // Still loading
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        // Protected screens
        composable(Screen.ProtectedDashboard.route) {
            ProtectedDashboardScreen(
                navController = navController,
                authViewModel = authViewModel,
                alertViewModel = alertViewModel
            )
        }

        composable(Screen.ProtectedHistory.route) {
            HistoryScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        composable(Screen.ProtectedTimeWindows.route) {
            pt.isec.a2022143267.safetysec.view.protected.TimeWindowsScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        composable(Screen.ProtectedMonitors.route) {
            pt.isec.a2022143267.safetysec.view.protected.MonitorsListScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        composable(Screen.ProtectedRules.route) {
            pt.isec.a2022143267.safetysec.view.protected.ProtectedRulesScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                navController = navController,
                authViewModel = authViewModel)
        }

        // Alert screen (shared)
        composable(
            route = Screen.AlertScreen.route,
            arguments = listOf(navArgument("alertId") { type = NavType.StringType })
        ) { backStackEntry ->
            val alertId = backStackEntry.arguments?.getString("alertId") ?: ""

            AlertScreen(
                navController = navController,
                alertId = alertId,
                authViewModel = authViewModel,
                alertViewModel = alertViewModel
            )
        }

        // Add more screens as needed
    }
}

