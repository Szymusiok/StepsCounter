package eu.tutorials.stepscounter.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import eu.tutorials.stepscounter.screens.settings.MountainHeader
import eu.tutorials.stepscounter.screens.settings.SettingsViewModel.DistanceUnit

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

    Scaffold(
        containerColor = Color(0xFFFDFBF9),
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            MountainHeader()

            Text(
                text = "Summary",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

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

@Composable
fun SummaryItem(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = Color.DarkGray)
        Text(value, style = MaterialTheme.typography.headlineSmall, color = Color.Black)
    }
}

