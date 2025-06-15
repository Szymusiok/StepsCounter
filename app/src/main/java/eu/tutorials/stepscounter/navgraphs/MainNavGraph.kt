package eu.tutorials.stepscounter.navgraphs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import eu.tutorials.stepscounter.screens.MainScreen
import eu.tutorials.stepscounter.screens.ProfileScreen
import eu.tutorials.stepscounter.screens.SummaryScreen
import eu.tutorials.stepscounter.screens.WorkoutDetailScreen
import eu.tutorials.stepscounter.screens.settings.SettingsScreen
import eu.tutorials.stepscounter.screens.settings.SettingsViewModel
import eu.tutorials.stepscounter.utils.Screen
import eu.tutorials.stepscounter.utils.MainScreen as MainRoutes

@Composable
fun MainFlowNavGraph(
    navController: NavHostController,
    settingsViewModel: SettingsViewModel
) {
    NavHost(
        navController = navController,
        startDestination = MainRoutes.HomeScreen.route
    ) {
        composable(MainRoutes.HomeScreen.route) {
            MainScreen(
                settingsViewModel = settingsViewModel,
                onNavigateToSettings = {
                    navController.navigate(MainRoutes.SettingsScreen.route)
                },
                onNavigateToProfile = {
                    navController.navigate(MainRoutes.ProfileScreen.route)
                },
                onNavigateToSummary = { totalDistance, steps, calories, elapsedTimeMs, pathPoints ->
                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.apply {
                            set("distance", totalDistance)
                            set("steps", steps)
                            set("calories", calories)
                            set("time", elapsedTimeMs)
                            set("path", pathPoints)
                        }
                    navController.navigate(MainRoutes.SummaryScreen.route)
                }
            )
        }

        composable(MainRoutes.ProfileScreen.route) {
            ProfileScreen(
                userEmail = FirebaseAuth.getInstance().currentUser?.email.orEmpty(),
                settingsViewModel = settingsViewModel,
                onBack = { navController.popBackStack() },
                onWorkoutClick = { workoutId ->
                    navController.navigate("workout_detail/$workoutId")
                }
            )
        }

        composable(MainRoutes.SummaryScreen.route) {
            val handle = navController.previousBackStackEntry?.savedStateHandle
            val distance = handle?.get<Double>("distance") ?: 0.0
            val steps = handle?.get<Int>("steps") ?: 0
            val calories = handle?.get<Int>("calories") ?: 0
            val timeMs = handle?.get<Long>("time") ?: 0L
            val pathPoints = handle?.get<List<LatLng>>("path") ?: emptyList()

            val distanceUnit by settingsViewModel.distanceUnit.collectAsState()

            SummaryScreen(
                totalDistance = distance,
                distanceUnit = distanceUnit,
                calories = calories,
                steps = steps,
                elapsedTimeMs = timeMs,
                pathPoints = pathPoints,
                onDone = {
                    navController.popBackStack(MainRoutes.HomeScreen.route, false)
                }
            )
        }

        composable(MainRoutes.SettingsScreen.route) {
            SettingsScreen(
                viewModel = settingsViewModel,
                onBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Screen.StartScreen.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "workout_detail/{workoutId}",
            arguments = listOf(navArgument("workoutId") { type = NavType.StringType })
        ) { backStackEntry ->
            val workoutId = backStackEntry.arguments?.getString("workoutId") ?: return@composable
            WorkoutDetailScreen(
                workoutId = workoutId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
