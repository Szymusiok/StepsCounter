package eu.tutorials.stepscounter.navgraphs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.maps.model.LatLng
import eu.tutorials.stepscounter.screens.MainScreen
import eu.tutorials.stepscounter.screens.settings.SettingsScreen
import eu.tutorials.stepscounter.screens.settings.SettingsViewModel
import eu.tutorials.stepscounter.screens.SummaryScreen
import eu.tutorials.stepscounter.utils.MainScreen

@Composable
fun MainFlowNavGraph(
    navController: NavHostController,
    settingsViewModel: SettingsViewModel
) {
    NavHost(
        navController = navController,
        startDestination = MainScreen.HomeScreen.route
    ) {
        // 1) Home / MainScreen
        composable(MainScreen.HomeScreen.route) {
            MainScreen(
                settingsViewModel = settingsViewModel,
                onNavigateToSettings = {
                    navController.navigate(MainScreen.SettingsScreen.route)
                },
                onNavigateToProfile = {
                    navController.navigate(MainScreen.ProfileScreen.route)
                },
                onNavigateToSummary = { totalDistance, steps, calories, elapsedTimeMs, pathPoints ->
                    // stash everything into the savedStateHandle
                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.apply {
                            set("distance", totalDistance)
                            set("steps", steps)
                            set("calories", calories)
                            set("time", elapsedTimeMs)
                            set("path", pathPoints)
                        }
                    navController.navigate(MainScreen.SummaryScreen.route)
                }
            )
        }

        // 2) Profile (stub)
        composable(MainScreen.ProfileScreen.route) {
            /* … */
        }

        // 3) SummaryScreen: pull the stats out of savedStateHandle
        composable(MainScreen.SummaryScreen.route) {
            val handle = navController
                .previousBackStackEntry
                ?.savedStateHandle
            // default to zero / empty if somehow missing
            val distance    = handle?.get<Double>("distance")    ?: 0.0
            val steps       = handle?.get<Int>("steps")          ?: 0
            val calories    = handle?.get<Int>("calories")      ?: 0
            val timeMs      = handle?.get<Long>("time")         ?: 0L
            val pathPoints  = handle?.get<List<LatLng>>("path") ?: emptyList()

            // get the user's preferred unit from the SettingsViewModel
            val distanceUnit by settingsViewModel.distanceUnit.collectAsState()

            SummaryScreen(
                totalDistance  = distance,
                distanceUnit   = distanceUnit,
                calories       = calories,
                steps          = steps,
                elapsedTimeMs  = timeMs,
                pathPoints     = pathPoints
            ) {
                // “Done” → back to home
                navController.popBackStack(
                    MainScreen.HomeScreen.route,
                    inclusive = false
                )
            }
        }

        // 4) Settings
        composable(MainScreen.SettingsScreen.route) {
            SettingsScreen(
                viewModel = settingsViewModel,
                onBack = {
                    navController.popBackStack()
                },
                onProfile = {
                    navController.navigate(MainScreen.ProfileScreen.route)
                },
                onAbout = { /*…*/ }
            )
        }
    }
}
