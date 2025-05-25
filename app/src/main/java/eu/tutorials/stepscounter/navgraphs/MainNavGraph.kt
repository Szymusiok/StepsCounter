package eu.tutorials.stepscounter.navgraphs

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import eu.tutorials.stepscounter.screens.MainScreen
import eu.tutorials.stepscounter.screens.settings.SettingsScreen
import eu.tutorials.stepscounter.screens.settings.SettingsViewModel
import eu.tutorials.stepscounter.utils.MainScreen

@Composable
fun MainFlowNavGraph(
    navController: NavHostController,
    settingsViewModel: SettingsViewModel
){
    NavHost(
        navController = navController,
        startDestination = MainScreen.HomeScreen.route
    ) {
        composable(MainScreen.HomeScreen.route) {
            MainScreen(
                settingsViewModel = settingsViewModel,
                onNavigateToSettings = {
                    navController.navigate(MainScreen.SettingsScreen.route)
                },
                onNavigateToProfile = {
                    navController.navigate(MainScreen.ProfileScreen.route)
                }
            )
        }
        composable(MainScreen.ProfileScreen.route) {
            //ProfileScreen()
        }
        composable(MainScreen.SettingsScreen.route) {
            SettingsScreen(
                viewModel = settingsViewModel,
                onBack = {
                    navController.navigate(MainScreen.HomeScreen.route)
                }
            )
        }
    }
}
