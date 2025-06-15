package eu.tutorials.stepscounter.screens

import android.Manifest
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import eu.tutorials.stepscounter.viewmodels.SettingsViewModel
import eu.tutorials.stepscounter.viewmodels.SettingsViewModel.DistanceUnit
import eu.tutorials.stepscounter.viewmodels.TrackingViewModel
import eu.tutorials.stepscounter.viewmodels.TrackingViewModelFactory
import eu.tutorials.stepscounter.ui.theme.KdamThmorPro

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainScreen(
    settingsViewModel: SettingsViewModel,
    trackingViewModel: TrackingViewModel = viewModel(
        factory = TrackingViewModelFactory(LocalContext.current)
    ),
    onNavigateToSettings: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSummary: (
        totalDistance: Double,
        steps: Int,
        calories: Int,
        elapsedTimeMs: Long,
        pathPoints: List<LatLng>
    ) -> Unit
) {
    val context = LocalContext.current

    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACTIVITY_RECOGNITION
        )
    } else {
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    val perms = rememberMultiplePermissionsState(permissions)
    LaunchedEffect(Unit) { perms.launchMultiplePermissionRequest() }

    val isTracking by trackingViewModel.isTracking.collectAsState()
    val pathPoints by trackingViewModel.pathPoints.collectAsState()
    val isPaused by trackingViewModel.isPaused.collectAsState()
    val rawMeters by trackingViewModel.totalDistance.collectAsState(initial = 0.0)
    val calories by trackingViewModel.calories.collectAsState(initial = 0.0)
    val elapsedMs by trackingViewModel.elapsedTime.collectAsState(initial = 0L)
    val steps by trackingViewModel.steps.collectAsState(initial = 0)
    val distanceUnit by settingsViewModel.distanceUnit.collectAsState()
    val speed by trackingViewModel.speed.collectAsState()
    val pace by trackingViewModel.pace.collectAsState()

    val hasFirstLocation = pathPoints.isNotEmpty()

    val displayDistance = when (distanceUnit) {
        DistanceUnit.KILOMETERS -> rawMeters / 1000.0
        DistanceUnit.MILES -> rawMeters / 1609.34
    }
    val unitLabel = if (distanceUnit == DistanceUnit.MILES) "mi" else "km"

    val speedDisplay = when (distanceUnit) {
        DistanceUnit.KILOMETERS -> speed
        DistanceUnit.MILES -> speed * 0.621371
    }
    val speedText = if (speedDisplay > 0)
        String.format("%.1f %s", speedDisplay, if (distanceUnit == DistanceUnit.MILES) "mph" else "km/h")
    else "--"

    val pacePerUnit = if (distanceUnit == DistanceUnit.KILOMETERS) pace else pace * 1.60934
    val paceMin = pacePerUnit.toInt()
    val paceSec = ((pacePerUnit - paceMin) * 60).toInt()
    val paceText = if (pacePerUnit > 0)
        String.format("%d:%02d / %s", paceMin, paceSec, unitLabel)
    else "--"

    val cameraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 1f)
    }

    val locationTracker = remember { LocationTracker(context) }
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    var locationLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(perms.allPermissionsGranted) {
        if (perms.allPermissionsGranted) {
            currentLocation = locationTracker.getCurrentLocation()
        }
    }

    LaunchedEffect(currentLocation) {
        currentLocation?.let { location ->
            val shiftedLatLng = LatLng(location.latitude - 0.005, location.longitude)
            cameraState.animate(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition.Builder()
                        .target(shiftedLatLng)
                        .zoom(15f)
                        .build()
                ),
                durationMs = 1000
            )
            locationLoaded = true // ✅ Now we trust that location is visible
        }
    }

    Box(Modifier.fillMaxSize()) {
        if (!locationLoaded) {
            // Show loading indicator over darkened background
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFFE37028))
            }
        }

        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraState,
            properties = MapProperties(isMyLocationEnabled = perms.allPermissionsGranted),
            uiSettings = MapUiSettings(myLocationButtonEnabled = true)
        ) {
            if (pathPoints.size > 1) {
                Polyline(points = pathPoints, color = Color(0xFF2E7D32), width = 5f)

                pathPoints.lastOrNull()?.let {
                    Marker(state = MarkerState(position = it))
                }
            }
        }

        // Bottom card
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color(0xFFFDFBF9), shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .padding(vertical = 16.dp, horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!isTracking) {
                Button(
                    onClick = { trackingViewModel.startTracking() },
                    enabled = currentLocation != null,
                    shape = RoundedCornerShape(30.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE37028),
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFFE37028).copy(alpha = 0.4f),
                        disabledContentColor = Color.White.copy(alpha = 0.7f)
                    ),
                    modifier = Modifier
                        .height(56.dp)
                        .width(180.dp)
                ) {
                    Text(
                        "Start",
                        style = MaterialTheme.typography.headlineSmall,
                        fontFamily = KdamThmorPro
                    )
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(
                        onClick = {
                            if (isPaused) trackingViewModel.resumeTracking() else trackingViewModel.pauseTracking()
                        },
                        shape = RoundedCornerShape(30.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE37028),
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .height(56.dp)
                            .width(120.dp)
                    ) {
                        Text(
                            if (isPaused) "Resume" else "Pause",
                            style = MaterialTheme.typography.headlineSmall,
                            fontFamily = KdamThmorPro
                        )
                    }
                    Button(
                        onClick = {
                            if (!hasFirstLocation) return@Button
                            trackingViewModel.stopTracking()
                            onNavigateToSummary(rawMeters, steps, calories.toInt(), elapsedMs, pathPoints)
                        },
                        shape = RoundedCornerShape(30.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE37028),
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .height(56.dp)
                            .width(120.dp)
                    ) {
                        Text(
                            "Stop",
                            style = MaterialTheme.typography.headlineSmall,
                            fontFamily = KdamThmorPro
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    StatItem("DURATION", formatTime(elapsedMs))
                    StatItem("DISTANCE", String.format("%.2f %s", displayDistance, unitLabel))
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    StatItem("CALORIES", "${calories.toInt()} kcal")
                    StatItem("STEPS", "$steps")
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    StatItem("SPEED", speedText)
                    StatItem("PACE", paceText)
                }
            }

            Divider(
                color = Color.LightGray,
                thickness = 1.dp,
                modifier = Modifier.padding(top = 8.dp)
            )

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconTextButton("Profile", Icons.Default.Person, onClick = onNavigateToProfile)
                VerticalDivider()
                IconTextButton("Settings", Icons.Default.Settings, onClick = onNavigateToSettings)
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = Color.DarkGray, fontFamily = KdamThmorPro)
        Text(value, style = MaterialTheme.typography.headlineSmall, color = Color.Black, fontFamily = KdamThmorPro)
    }
}

@Composable
private fun IconTextButton(text: String, icon: ImageVector, onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = text, tint = Color.Black)
            Text(text, color = Color.Black, style = MaterialTheme.typography.bodyMedium, fontFamily = KdamThmorPro)
        }
    }
}

@Composable
private fun VerticalDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(36.dp)
            .background(Color.LightGray)
    )
}

private fun formatTime(ms: Long): String {
    val totalSec = ms / 1000
    val hrs = totalSec / 3600
    val min = (totalSec % 3600) / 60
    val sec = totalSec % 60
    return String.format("%02d:%02d:%02d", hrs, min, sec)
}