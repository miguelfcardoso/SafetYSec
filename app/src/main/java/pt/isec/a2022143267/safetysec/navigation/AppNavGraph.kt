package pt.isec.a2022143267.safetysec.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import pt.isec.a2022143267.safetysec.model.UserType
import pt.isec.a2022143267.safetysec.view.auth.LoginScreen
import pt.isec.a2022143267.safetysec.view.auth.RegisterScreen
import pt.isec.a2022143267.safetysec.view.auth.ForgotPasswordScreen
import pt.isec.a2022143267.safetysec.view.monitor.MonitorDashboardScreen
import pt.isec.a2022143267.safetysec.view.protected.ProtectedDashboardScreen
import pt.isec.a2022143267.safetysec.view.protected.HistoryScreen
import pt.isec.a2022143267.safetysec.view.alert.AlertScreen
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

        // Protected screens
        composable(Screen.ProtectedDashboard.route) {
            ProtectedDashboardScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        composable(Screen.ProtectedHistory.route) {
            HistoryScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        // Alert screen (shared)
        composable(Screen.AlertScreen.route) { backStackEntry ->
            val alertId = backStackEntry.arguments?.getString("alertId") ?: ""
            AlertScreen(
                navController = navController,
                alertId = alertId,
                authViewModel = authViewModel
            )
        }

        // Add more screens as needed
    }
}

