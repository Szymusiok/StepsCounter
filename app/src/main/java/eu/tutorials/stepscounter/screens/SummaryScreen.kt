package eu.tutorials.stepscounter.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import eu.tutorials.stepscounter.screens.settings.SettingsViewModel.DistanceUnit

@SuppressLint("DefaultLocale")
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
        DistanceUnit.MILES      -> totalDistance / 1609.34
    }
    val unitLabel    = if (distanceUnit == DistanceUnit.MILES) "mi" else "km"
    val distanceText = String.format("%.2f %s", displayDistance, unitLabel)

    val totalSec = (elapsedTimeMs / 1000)
    val hrs      = totalSec / 3600
    val mins     = (totalSec % 3600) / 60
    val secs     = totalSec % 60
    val timeText = String.format("%02d:%02d:%02d", hrs, mins, secs)

    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val mapHeight    = screenHeight * 0.30f

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Summary", style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor    = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            Button(
                onClick = onDone,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape  = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Done", color = MaterialTheme.colorScheme.onPrimary, fontSize = 18.sp)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            if (pathPoints.isNotEmpty()) {
                val cameraState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(pathPoints.first(), 14f)
                }
                Card(
                    modifier   = Modifier
                        .fillMaxWidth()
                        .height(mapHeight),
                    shape      = RoundedCornerShape(16.dp),
                    elevation  = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    GoogleMap(
                        modifier            = Modifier.fillMaxSize(),
                        cameraPositionState = cameraState,
                        uiSettings          = MapUiSettings(
                            zoomControlsEnabled   = false,
                            scrollGesturesEnabled = false
                        ),
                        properties          = MapProperties(mapType = MapType.NORMAL)
                    ) {
                        if (pathPoints.size > 1) {
                            Polyline(points = pathPoints, width = 6f)
                        }
                        Marker(state = MarkerState(pathPoints.first()), title = "Start")
                        Marker(state = MarkerState(pathPoints.last()),  title = "End")
                    }
                }
            }

            Card(
                modifier  = Modifier.fillMaxWidth(),
                shape     = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier            = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier            = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(Icons.Default.LocationOn, "Distance", distanceText)
                        StatItem(Icons.Default.DateRange,   "Time",     timeText)
                    }

                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))

                    Row(
                        modifier            = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(Icons.Default.Info, "Calories", "$calories kcal")
                        StatItem(Icons.Default.Build,       "Steps",    "$steps")
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment   = Alignment.CenterHorizontally,
        verticalArrangement   = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector     = icon,
            contentDescription = label,
            modifier         = Modifier.size(32.dp),
            tint             = MaterialTheme.colorScheme.primary
        )
        Text(label, style = MaterialTheme.typography.labelMedium)
        Text(value, style = MaterialTheme.typography.titleMedium)
    }
}
