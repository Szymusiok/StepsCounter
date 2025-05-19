package eu.tutorials.stepscounter.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.extractor.mp4.Track
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import eu.tutorials.stepscounter.LocationTracker
import eu.tutorials.stepscounter.screens.settings.SettingsScreen
import eu.tutorials.stepscounter.screens.settings.SettingsViewModel
import eu.tutorials.stepscounter.viewmodels.StepsViewModel
import eu.tutorials.stepscounter.viewmodels.TrackingViewModel
import eu.tutorials.stepscounter.viewmodels.TrackingViewModelFactory


@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    stepsViewModel: StepsViewModel = viewModel(),
    onNavigateToSettings: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val ctx = LocalContext.current

    val trackingFactory = remember { TrackingViewModelFactory(ctx,stepsViewModel) }
    val trackingViewModel: TrackingViewModel = viewModel(factory = trackingFactory)

    // 1️⃣ Permissions
    val permState = rememberMultiplePermissionsState(
        permissions = listOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    LaunchedEffect(Unit) {
        permState.launchMultiplePermissionRequest()
    }

    // 2️⃣ Camera state with a default world view:
    val defaultLatLng = LatLng(0.0, 0.0)
    val cameraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLatLng, 1f)
    }

    val isTracking  by trackingViewModel.isTracking.collectAsState()
    val pathPoints  by trackingViewModel.pathPoints.collectAsState()
    val distanceM   by trackingViewModel.totalDistance.collectAsState()
    val calories    by trackingViewModel.calories.collectAsState()
    val elapsedMs   by trackingViewModel.elapsedTime.collectAsState()
    val steps       by stepsViewModel.steps.collectAsState(initial = 0)
    var isHiking by remember { mutableStateOf(false) }

    // 3️⃣ Hold onto the real device location
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }

    // 4️⃣ Fetch location once permissions granted
    LaunchedEffect(permState.allPermissionsGranted) {
        if (permState.allPermissionsGranted) {
            LocationTracker(ctx).getCurrentLocation()?.also { loc ->
                currentLocation = loc
            }
        }
    }

    // 5️⃣ Animate camera to currentLocation when we get it
    LaunchedEffect(currentLocation) {
        currentLocation?.let { latLng ->
            cameraState.animate(
                update = CameraUpdateFactory.newLatLngZoom(latLng, 15f),
                durationMs = 1_000
            )
        }
    }

    // 6️⃣ The UI
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
            DisposableEffect(ctx) {
                com.google.android.gms.maps.MapsInitializer.initialize(ctx.applicationContext)
                onDispose { }
            }

            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraState,
                properties = MapProperties(
                    isMyLocationEnabled = permState.allPermissionsGranted
                ),
                uiSettings = MapUiSettings(myLocationButtonEnabled = true)
            ) {
                // draw your path
                if (pathPoints.size > 1) {
                    Polyline(points = pathPoints, width = 5f)
                }
                // current location marker
                pathPoints.lastOrNull()?.let {
                    Marker(state = MarkerState(position = it), title = "You")
                }
            }

            if (!permState.allPermissionsGranted) {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Please grant location permission")
                }
            }

            // Top info card
            Card(
                Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
                    .fillMaxWidth(0.9f),
                elevation = CardDefaults.cardElevation(8.dp),
                shape = RoundedCornerShape(8.dp)
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
                        "Distance" to String.format("%.2f km", distanceM / 1000f),
                        "Time"     to formattedTime,
                        "Calories" to "${calories.toInt()} kcal",
                        "Steps"    to "$steps steps"
                    ).forEach { (label, value) ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(label)
                            Text(value, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }

            // Start Button
            // Start Button
            Button(
                onClick = {
                    // STEPS PART
                    if (isHiking) {
                        stepsViewModel.stopTracking()
                        trackingViewModel.stopTracking()
                    } else {
                        stepsViewModel.startTracking()
                        trackingViewModel.startTracking()
                    }
                    isHiking = !isHiking
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(24.dp)
                    .fillMaxWidth(0.6f),
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    if (isHiking) "Stop Hiking"
                    else "Start Hiking"
                )
            }

        }
    }
}
