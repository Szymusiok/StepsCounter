package eu.tutorials.stepscounter.navgraphs

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import eu.tutorials.stepscounter.viewmodels.AuthViewModel
import eu.tutorials.stepscounter.utils.Screen
import eu.tutorials.stepscounter.screens.LoginScreen
import eu.tutorials.stepscounter.screens.SignUpScreen
import eu.tutorials.stepscounter.screens.StartScreen
import eu.tutorials.stepscounter.viewmodels.SettingsViewModel

// Navigation graph for login and sign-up screens
fun isAuthRoute(route: String?) = route in listOf(
    Screen.StartScreen.route,
    Screen.LoginScreen.route,
    Screen.SignupScreen.route
)

// Navigation graph for the sign in flow
@Composable
fun StartNavigationGraph(
    modifier: Modifier,
    navController: NavHostController,
    authViewModel: AuthViewModel,
    settingsViewModel: SettingsViewModel
) {

    // Top level host for auth screens
    NavHost(
        navController = navController,
        startDestination = Screen.StartScreen.route
    ) {
        composable(Screen.StartScreen.route) {
            StartScreen(
                onNavigateToLogin = { navController.navigate(Screen.LoginScreen.route)},
                onNavigateToSignUp = { navController.navigate(Screen.SignupScreen.route)},
                onContinueOffline = {
                    navController.navigate(Screen.MainFlow.route) {
                        popUpTo(Screen.StartScreen.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                modifier = modifier
            )
        }
        composable(Screen.SignupScreen.route) {
            SignUpScreen(
                authViewModel = authViewModel,
                onNavigateToLogin = { navController.navigate(Screen.LoginScreen.route) },
                onSignUpSuccess = {
                    navController.navigate(Screen.MainFlow.route) {
                        popUpTo(Screen.StartScreen.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(Screen.LoginScreen.route) {
            LoginScreen(
                authViewModel = authViewModel,
                onNavigateToSignUp = { navController.navigate(Screen.SignupScreen.route) },
                onSignInSuccess = {
                    navController.navigate(Screen.MainFlow.route){
                        popUpTo(Screen.StartScreen.route){
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                onContinueOffline = {
                    navController.navigate(Screen.MainFlow.route){
                        popUpTo(Screen.StartScreen.route){
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(Screen.MainFlow.route) {
            MainFlowNavGraph(
                navController = rememberNavController(),
                settingsViewModel = settingsViewModel
            )
        }
    }
}