package eu.tutorials.stepscounter.navgraphs

import android.net.Uri
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

fun isAuthRoute(route: String?) = route in listOf(
    Screen.StartScreen.route,
    Screen.LoginScreen.route,
    Screen.SignupScreen.route
)

@Composable
fun StartNavigationGraph(
    modifier: Modifier,
    navController: NavHostController,
    authViewModel: AuthViewModel,
) {
    NavHost(
        navController = navController,
        startDestination = Screen.StartScreen.route
    ) {
        composable(Screen.StartScreen.route) {
            StartScreen(
                onNavigateToLogin = { navController.navigate(Screen.LoginScreen.route)},
                onNavigateToSignUp = { navController.navigate(Screen.SignupScreen.route)},
                modifier = modifier
            )
        }
        composable(Screen.SignupScreen.route) {
            SignUpScreen(
                authViewModel = authViewModel,
                onNavigateToLogin = { navController.navigate(Screen.LoginScreen.route) }
            )
        }
        composable(Screen.LoginScreen.route) {
            LoginScreen(
                authViewModel = authViewModel,
                onNavigateToSignUp = { navController.navigate(Screen.SignupScreen.route) }
            ){
                navController.navigate(Screen.MainFlow.route)
            }
        }
        composable(Screen.MainFlow.route) {
            MainFlowNavGraph(
                navController = rememberNavController()
            )
        }
    }
}