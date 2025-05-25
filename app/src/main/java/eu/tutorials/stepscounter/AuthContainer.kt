package eu.tutorials.stepscounter

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.compose.currentBackStackEntryAsState
import eu.tutorials.stepscounter.viewmodels.AuthViewModel
import eu.tutorials.stepscounter.utils.AuthVideoBackground
import eu.tutorials.stepscounter.navgraphs.StartNavigationGraph
import eu.tutorials.stepscounter.navgraphs.isAuthRoute
import eu.tutorials.stepscounter.screens.settings.SettingsViewModel

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
    // 1) Watch the nav back-stack for route changes
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    // 2) Only show the video on auth-related screens
    val showVideo = isAuthRoute(currentRoute)

    Box(modifier.fillMaxSize()) {
        if (showVideo) {
            AuthVideoBackground(videoUri = videoUri)
        }

        // 3) Drive your start/login/signup/main graph here
        StartNavigationGraph(
            modifier      = modifier,
            navController = navController,
            authViewModel = authViewModel,
            settingsViewModel = settingsViewModel
        )
    }
}