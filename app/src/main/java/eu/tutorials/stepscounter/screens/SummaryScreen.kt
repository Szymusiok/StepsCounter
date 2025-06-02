package eu.tutorials.stepscounter.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import eu.tutorials.stepscounter.databasehelpers.UserRepository
import eu.tutorials.stepscounter.model.Workout
import eu.tutorials.stepscounter.screens.settings.SettingsViewModel.DistanceUnit
import com.google.firebase.Timestamp
import eu.tutorials.stepscounter.KdamThmorPro
import eu.tutorials.stepscounter.utils.MountainHeaderFullScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryScreen(
    totalDistance: Double,
    distanceUnit: DistanceUnit,
    calories: Int,
    steps: Int,
    elapsedTimeMs: Long,
    pathPoints: List<LatLng>,
    onDone: () -> Unit
) {
    val displayDistance = when (distanceUnit) {
        DistanceUnit.KILOMETERS -> totalDistance / 1000.0
        DistanceUnit.MILES -> totalDistance / 1609.34
    }
    val unitLabel = if (distanceUnit == DistanceUnit.MILES) "mi" else "km"
    val distanceText = String.format("%.2f", displayDistance)

    val totalSec = elapsedTimeMs / 1000
    val hrs = totalSec / 3600
    val mins = (totalSec % 3600) / 60
    val secs = totalSec % 60
    val timeText = String.format("%d:%02d", hrs * 60 + mins, secs)

    val avgPace = if (displayDistance > 0) {
        val paceSec = totalSec / displayDistance
        val paceMin = (paceSec / 60).toInt()
        val paceRemainder = (paceSec % 60).toInt()
        String.format("%d:%02d/%s", paceMin, paceRemainder, unitLabel)
    } else "--"

    val userRepo = remember {
        UserRepository(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())
    }
    val userEmail = FirebaseAuth.getInstance().currentUser?.email

    LaunchedEffect(Unit) {
        userEmail?.let {
            val workout = Workout(
                path = pathPoints,
                distanceMeters = totalDistance,
                steps = steps,
                calories = calories,
                durationMs = elapsedTimeMs,
                timestamp = Timestamp.now()
            )
            userRepo.saveWorkout(it, workout)
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            Button(
                onClick = onDone,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE37028),
                    contentColor = Color.White
                )
            ) {
                Text("Done", style = MaterialTheme.typography.titleMedium)
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            MountainHeaderFullScreen(modifier = Modifier.matchParentSize())

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 80.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                Spacer(modifier = Modifier.weight(1f)) // Push content to bottom

                // Title
                Text(
                    text = "Summary",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(horizontal = 24.dp),
                    fontFamily = KdamThmorPro
                )
                Spacer(Modifier.height(16.dp))
                // Map
                if (pathPoints.isNotEmpty()) {
                    val cameraState = rememberCameraPositionState {
                        position = CameraPosition.fromLatLngZoom(pathPoints.first(), 14f)
                    }

                    Card(
                        modifier = Modifier
                            .padding(horizontal = 24.dp)
                            .fillMaxWidth()
                            .aspectRatio(1.3f),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        GoogleMap(
                            modifier = Modifier.fillMaxSize(),
                            cameraPositionState = cameraState,
                            uiSettings = MapUiSettings(zoomControlsEnabled = false, scrollGesturesEnabled = false),
                            properties = MapProperties(mapType = MapType.NORMAL)
                        ) {
                            if (pathPoints.size > 1) {
                                Polyline(points = pathPoints, width = 6f)
                            }
                            Marker(state = MarkerState(pathPoints.last()))
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                // Stats
                Card(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            SummaryItem("Distance", "$distanceText $unitLabel")
                            SummaryItem("Time", timeText)
                        }
                        Spacer(Modifier.height(16.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            SummaryItem("Calories", "$calories kcal")
                            SummaryItem("Steps", steps.toString())
                        }
                        Divider(Modifier.padding(vertical = 16.dp), color = Color.LightGray)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Avg. Pace", style = MaterialTheme.typography.labelMedium, color = Color.DarkGray)
                            Text(avgPace, style = MaterialTheme.typography.headlineSmall, color = Color.Black)
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun SummaryItem(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = Color.DarkGray)
        Text(value, style = MaterialTheme.typography.headlineSmall, color = Color.Black)
    }
}

