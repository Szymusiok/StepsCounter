package eu.tutorials.stepscounter.navgraphs

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import eu.tutorials.stepscounter.screens.MainScreen
import eu.tutorials.stepscounter.utils.MainScreen

@Composable
fun MainFlowNavGraph(
    navController: NavHostController,
){

    val mainNavController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = MainScreen.HomeScreen.route
    ) {
        composable(MainScreen.HomeScreen.route) {
            MainScreen()
            //HomeScreen(
              //  onNavigateToDetail = { mainNavController.navigate(MainScreen.Detail.route) },
               // onNavigateToSettings = { mainNavController.navigate(MainScreen.Settings.route) }
           // )
        }
        composable(MainScreen.DetailScreen.route) {
            //DetailScreen(
              //  onBack = { mainNavController.popBackStack() }
           // )
        }
        composable(MainScreen.SettingsScreen.route) {
            //SettingsScreen(
            //    onBack = { mainNavController.popBackStack() }
            //)
        }
    }
}
