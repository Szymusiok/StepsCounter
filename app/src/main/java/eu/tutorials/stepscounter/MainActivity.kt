package eu.tutorials.stepscounter

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import eu.tutorials.stepscounter.viewmodels.SettingsViewModel
import eu.tutorials.stepscounter.ui.theme.StepsCounterTheme
import eu.tutorials.stepscounter.viewmodels.AuthViewModel
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

// Main entry point. Hides the system UI and loads our Compose screens.
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Let our content go behind the status and nav bars
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()

        // Hide the bars
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.systemBars())
        }

        setContent {
            val navController = rememberNavController()
            val authViewModel: AuthViewModel = viewModel()
            val settingsViewModel: SettingsViewModel = viewModel()

            StepsCounterTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AuthContainer(
                        videoUri = getVideoUri(),
                        modifier = Modifier.padding(innerPadding),
                        navController = navController,
                        authViewModel = authViewModel,
                        settingsViewModel = settingsViewModel
                    )
                }
            }
        }
    }

    // Build the URI to the intro video in res/raw
    private fun getVideoUri(): Uri {
        val rawId = resources.getIdentifier("trekking", "raw", packageName)
        return Uri.parse("android.resource://$packageName/$rawId")
    }
}
