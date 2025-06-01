package eu.tutorials.stepscounter.screens

import android.icu.text.SimpleDateFormat
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import eu.tutorials.stepscounter.utils.MountainHeaderFullScreen
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.CameraPosition
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.android.compose.*
import eu.tutorials.stepscounter.KdamThmorPro
import eu.tutorials.stepscounter.databasehelpers.UserRepository
import eu.tutorials.stepscounter.model.Workout
import java.util.*

@Composable
fun WorkoutDetailScreen(
    workoutId: String,
    userRepository: UserRepository = remember {
        UserRepository(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())
    },
    onBack: () -> Unit
) {
    var workout by remember { mutableStateOf<Workout?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(workoutId) {
        workout = userRepository.getWorkoutById(workoutId)
        isLoading = false
    }

    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        workout?.let { wk ->
            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(
                    wk.path.firstOrNull() ?: return@rememberCameraPositionState,
                    15f
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                MountainHeaderFullScreen() // your enhanced gradient mountain background

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        "Workout",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        fontFamily = KdamThmorPro
                    )

                    Text(
                        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(wk.timestamp.toDate()),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        color = Color.Gray,
                        fontFamily = KdamThmorPro
                    )

                    Spacer(Modifier.height(16.dp))

                    Card(
                        modifier = Modifier
                            .padding(horizontal = 24.dp)
                            .fillMaxWidth()
                            .aspectRatio(1.3f),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        GoogleMap(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp),
                            cameraPositionState = cameraPositionState,
                            uiSettings = MapUiSettings(zoomControlsEnabled = false),
                            properties = MapProperties(isMyLocationEnabled = false)
                        ) {
                            if (wk.path.size > 1) {
                                Polyline(points = wk.path, color = Color.Red, width = 5f)
                                Marker(
                                    state = MarkerState(position = wk.path.first()),
                                    title = "Start"
                                )
                                Marker(
                                    state = MarkerState(position = wk.path.last()),
                                    title = "End"
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, shape = RoundedCornerShape(24.dp))
                            .padding(24.dp)
                    ) {
                        SummaryRow("Distance", "%.2f km".format(wk.distanceMeters / 1000))
                        SummaryRow("Duration", formatDuration(wk.durationMs))
                        SummaryRow("Calories", "${wk.calories} kcal")
                        SummaryRow("Steps", "${wk.steps}")
                        val paceSec = if (wk.distanceMeters > 0) wk.durationMs / (wk.distanceMeters / 1000) else 0.0
                        val paceMin = (paceSec / 60).toInt()
                        val paceRemainder = (paceSec % 60).toInt()
                        SummaryRow("Avg. Pace", "$paceMin:${paceRemainder.toString().padStart(2, '0')} min/km")
                    }

                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d:%02d", hours, minutes, seconds)
}

@Composable
fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 18.sp,
            color = Color.DarkGray,
            fontFamily = KdamThmorPro
        )
        Text(
            text = value,
            fontSize = 18.sp,
            color = Color.Black,
            fontFamily = KdamThmorPro
        )
    }
}


