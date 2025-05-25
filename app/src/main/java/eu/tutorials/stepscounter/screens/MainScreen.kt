package eu.tutorials.stepscounter.screens

import android.Manifest
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import eu.tutorials.stepscounter.LocationTracker
import eu.tutorials.stepscounter.screens.settings.SettingsViewModel
import eu.tutorials.stepscounter.screens.settings.SettingsViewModel.AppTheme
import eu.tutorials.stepscounter.screens.settings.SettingsViewModel.DistanceUnit
import eu.tutorials.stepscounter.viewmodels.TrackingViewModel
import eu.tutorials.stepscounter.viewmodels.TrackingViewModelFactory
import eu.tutorials.stepscounter.viewmodels.StepsViewModel

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    settingsViewModel: SettingsViewModel,
    stepsViewModel: StepsViewModel = viewModel(),
    trackingViewModel: TrackingViewModel = viewModel(
        factory = TrackingViewModelFactory(LocalContext.current, stepsViewModel)
    ),
    onNavigateToSettings: () -> Unit,
    onNavigateToProfile: () -> Unit,
) {
    val ctx = LocalContext.current

    // 1) Permissions
    val perms = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )
    LaunchedEffect(Unit) { perms.launchMultiplePermissionRequest() }

    // 2) Collect VM state
    val isTracking  by trackingViewModel.isTracking.collectAsState()
    val pathPoints  by trackingViewModel.pathPoints.collectAsState()
    val rawDistance by trackingViewModel.totalDistance.collectAsState(initial = 0.0)
    val calories    by trackingViewModel.calories.collectAsState(initial = 0.0)
    val elapsedMs   by trackingViewModel.elapsedTime.collectAsState(initial = 0L)

    // **Collect steps from the StepsViewModel**:
    val steps by stepsViewModel.steps.collectAsState(initial = 0)

    // 3) Settings state
    val distanceUnit by settingsViewModel.distanceUnit.collectAsState()
    val appTheme     by settingsViewModel.appTheme.collectAsState()

    // 4) Apply theme immediately
    SideEffect {
        when (appTheme) {
            AppTheme.LIGHT  -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            AppTheme.DARK   -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            AppTheme.SYSTEM -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    // Convert raw meters → km or mi
    val rawMeters by trackingViewModel.totalDistance.collectAsState(initial = 0.0)
    val displayDistance = when (distanceUnit) {
        SettingsViewModel.DistanceUnit.KILOMETERS -> rawMeters / 1000.0
        SettingsViewModel.DistanceUnit.MILES      -> rawMeters / 1609.34
    }
    val unitLabel = if (distanceUnit == SettingsViewModel.DistanceUnit.MILES) "mi" else "km"

    // 6) Map + one-shot camera
    val cameraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 1f)
    }
    val locationTracker = remember { LocationTracker(ctx) }
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    LaunchedEffect(perms.allPermissionsGranted) {
        if (perms.allPermissionsGranted) {
            currentLocation = locationTracker.getCurrentLocation()
        }
    }
    LaunchedEffect(currentLocation) {
        currentLocation?.let {
            cameraState.animate(
                update     = CameraUpdateFactory.newLatLngZoom(it, 15f),
                durationMs = 1_000
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("GŁÓWNY SCREEN") },
                actions = {
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            GoogleMap(
                modifier            = Modifier.fillMaxSize(),
                cameraPositionState = cameraState,
                properties          = MapProperties(isMyLocationEnabled = perms.allPermissionsGranted),
                uiSettings          = MapUiSettings(myLocationButtonEnabled = true)
            ) {
                if (pathPoints.size > 1) Polyline(points = pathPoints, width = 5f)
                pathPoints.lastOrNull()?.let {
                    Marker(state = MarkerState(position = it), title = "You")
                }
            }

            if (!perms.allPermissionsGranted) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Please grant location permission")
                }
            }

            Card(
                modifier  = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
                    .fillMaxWidth(0.9f),
                elevation = CardDefaults.cardElevation(8.dp),
                shape     = RoundedCornerShape(8.dp)
            ) {
                val formattedTime = String.format(
                    "%02d:%02d",
                    (elapsedMs / 1000) / 60,
                    (elapsedMs / 1000) % 60
                )
                Row(
                    Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf(
                        "Distance" to String.format("%.2f %s", displayDistance, unitLabel),
                        "Time"     to formattedTime,
                        "Calories" to "${calories.toInt()} kcal",
                        "Steps"    to "$steps"
                    ).forEach { (label, value) ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(label)
                            Text(value, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }

            Button(
                onClick = {
                    if (isTracking) trackingViewModel.stopTracking()
                    else            trackingViewModel.startTracking()
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(24.dp)
                    .fillMaxWidth(0.6f),
                shape = RoundedCornerShape(50)
            ) {
                Text(if (isTracking) "Stop Hiking" else "Start Hiking")
            }
        }
    }
}