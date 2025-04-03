package eu.tutorials.stepscounter

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun NavigationGraph(
    videoUri: Uri,
    modifier: Modifier,
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.StartScreen.route
    ) {
        composable(Screen.StartScreen.route) {
            StartScreen(
                videoUri = videoUri,
                onNavigateToLogin = { navController.navigate(Screen.LoginScreen.route)},
                onNavigateToSignUp = { navController.navigate(Screen.SignupScreen.route)},
                modifier = modifier
            )
        }
        composable(Screen.SignupScreen.route) {
            SignUpScreen(
                videoUri = videoUri,
                authViewModel = authViewModel,
                onNavigateToLogin = { navController.navigate(Screen.LoginScreen.route) }
            )
        }
        composable(Screen.LoginScreen.route) {
            LoginScreen(
                videoUri = videoUri,
                authViewModel = authViewModel,
                onNavigateToSignUp = { navController.navigate(Screen.SignupScreen.route) }
            ){
                navController.navigate(Screen.MainScreen.route)
            }
        }
        composable(Screen.MainScreen.route) {
            MainScreen()
        }
    }
}