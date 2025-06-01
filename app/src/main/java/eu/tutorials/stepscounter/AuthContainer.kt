package eu.tutorials.stepscounter

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import eu.tutorials.stepscounter.navgraphs.StartNavigationGraph
import eu.tutorials.stepscounter.navgraphs.isAuthRoute
import eu.tutorials.stepscounter.screens.settings.SettingsViewModel
import eu.tutorials.stepscounter.utils.AuthVideoBackground
import eu.tutorials.stepscounter.viewmodels.AuthViewModel

val KdamThmorPro = FontFamily(
    Font(R.font.kdam_thmor_pro, FontWeight.Normal)
)

@Composable
fun AuthContainer(
    videoUri: Uri,
    modifier: Modifier = Modifier,
    navController: NavHostController,
    authViewModel: AuthViewModel,
    settingsViewModel: SettingsViewModel
) {
    // Watch route changes
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val showVideo = isAuthRoute(currentRoute)

    // Fullscreen container with system bar padding
    Box(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {
        if (showVideo) {
            AuthVideoBackground(videoUri = videoUri)
        }

        // Navigation graph host
        StartNavigationGraph(
            modifier = Modifier.fillMaxSize(),
            navController = navController,
            authViewModel = authViewModel,
            settingsViewModel = settingsViewModel
        )
    }
}
