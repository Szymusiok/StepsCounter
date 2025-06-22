package eu.tutorials.stepscounter

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import eu.tutorials.stepscounter.navgraphs.StartNavigationGraph
import eu.tutorials.stepscounter.navgraphs.isAuthRoute
import eu.tutorials.stepscounter.viewmodels.SettingsViewModel
import eu.tutorials.stepscounter.utils.AuthVideoBackground
import eu.tutorials.stepscounter.viewmodels.AuthViewModel

// Wrapper around the login and sign up screens.
// Displays a looping video when those screens are shown
@Composable
fun AuthContainer(
    videoUri: Uri,
    modifier: Modifier = Modifier,
    navController: NavHostController,
    authViewModel: AuthViewModel,
    settingsViewModel: SettingsViewModel
) {
    // Figure out which screen we are on to decide if the video should play
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val showVideo = isAuthRoute(currentRoute)

    // Fill the screen and account for the system bars
    Box(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {
        if (showVideo) {
            AuthVideoBackground(videoUri = videoUri)
        }

        // login/signup navigation
        StartNavigationGraph(
            modifier = Modifier.fillMaxSize(),
            navController = navController,
            authViewModel = authViewModel,
            settingsViewModel = settingsViewModel
        )
    }
}
