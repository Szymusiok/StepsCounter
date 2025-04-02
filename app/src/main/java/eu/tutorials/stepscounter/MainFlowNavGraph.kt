package eu.tutorials.stepscounter

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun MainFlowNavGraph(
    navController: NavHostController,
){
    // This NavHost manages all main flow screens (e.g., Home, Detail, Settings).

    val mainNavController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = MainScreen.HomeScreen.route
    ) {
        composable(MainScreen.HomeScreen.route) {
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
