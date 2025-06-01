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
import eu.tutorials.stepscounter.screens.settings.SettingsViewModel
import eu.tutorials.stepscounter.ui.theme.StepsCounterTheme
import eu.tutorials.stepscounter.viewmodels.AuthViewModel
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Enable drawing behind system bars
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()

        // ✅ HIDE system bars (immersive mode)
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

    private fun getVideoUri(): Uri {
        val rawId = resources.getIdentifier("trekking", "raw", packageName)
        return Uri.parse("android.resource://$packageName/$rawId")
    }
}
