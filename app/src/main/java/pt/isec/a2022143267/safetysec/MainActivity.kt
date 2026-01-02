package pt.isec.a2022143267.safetysec

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import pt.isec.a2022143267.safetysec.data.ThemePreferences
import pt.isec.a2022143267.safetysec.navigation.AppNavGraph
import pt.isec.a2022143267.safetysec.ui.theme.SafetYSecTheme
import pt.isec.a2022143267.safetysec.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    private lateinit var themePreferences: ThemePreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        themePreferences = ThemePreferences(this)

        setContent {
            val isDarkMode by themePreferences.isDarkMode.collectAsState(initial = false)

            SafetYSecTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val authViewModel: AuthViewModel = viewModel()

                    AppNavGraph(
                        navController = navController,
                        authViewModel = authViewModel
                    )
                }
            }
        }
    }
}
