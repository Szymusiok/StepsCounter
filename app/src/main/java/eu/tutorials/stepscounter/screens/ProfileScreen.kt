package eu.tutorials.stepscounter.screens

import android.icu.text.SimpleDateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import eu.tutorials.stepscounter.utils.MountainHeaderFullScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import eu.tutorials.stepscounter.KdamThmorPro
import eu.tutorials.stepscounter.databasehelpers.Result
import eu.tutorials.stepscounter.databasehelpers.User
import eu.tutorials.stepscounter.databasehelpers.UserRepository
import eu.tutorials.stepscounter.model.Workout
import java.util.*

@Composable
fun ProfileScreen(
    userEmail: String,
    userRepository: UserRepository = remember {
        UserRepository(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())
    },
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onWorkoutClick: (String) -> Unit
) {
    var user by remember { mutableStateOf<User?>(null) }
    var workouts by remember { mutableStateOf<List<Workout>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(userEmail) {
        when (val result = userRepository.getUserData(userEmail)) {
            is Result.Success -> user = result.data
            is Result.Error -> {}
        }
        workouts = userRepository.getWorkouts(userEmail)
        isLoading = false
    }

    Box(modifier = modifier.fillMaxSize()) {
        MountainHeaderFullScreen(modifier = Modifier.matchParentSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 48.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.Start)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                user?.let {
                    Text(
                        text = "${it.firstName} ${it.lastName}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontFamily = KdamThmorPro,
                        color = Color.Black
                    )
                }
                Text(
                    userEmail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray
                )
            }

            Divider(modifier = Modifier.padding(horizontal = 24.dp))

            Text(
                "Past Workouts",
                modifier = Modifier.padding(start = 24.dp, top = 12.dp, bottom = 8.dp),
                style = MaterialTheme.typography.titleMedium,
                fontFamily = KdamThmorPro
            )

            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(workouts) { workout ->
                        WorkoutSummaryCard(workout) {
                            onWorkoutClick(workout.id)
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun WorkoutSummaryCard(workout: Workout, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Date: ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(workout.timestamp.toDate())}")
            Text("Distance: ${"%.2f".format(workout.distanceMeters / 1000)} km")
            Text("Duration: ${formatDuration(workout.durationMs)}")
        }
    }
}
