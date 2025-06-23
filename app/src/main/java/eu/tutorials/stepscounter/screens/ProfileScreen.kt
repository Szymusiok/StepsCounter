package eu.tutorials.stepscounter.screens

import android.icu.text.SimpleDateFormat
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
import eu.tutorials.stepscounter.ui.theme.KdamThmorPro
import eu.tutorials.stepscounter.databasehelpers.Result
import eu.tutorials.stepscounter.databasehelpers.User
import eu.tutorials.stepscounter.databasehelpers.UserRepository
import eu.tutorials.stepscounter.model.Workout
import java.util.*
import androidx.compose.ui.platform.LocalContext
import eu.tutorials.stepscounter.databasehelpers.AppDatabase
import eu.tutorials.stepscounter.viewmodels.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userEmail: String,
    settingsViewModel: SettingsViewModel,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onWorkoutClick: (String) -> Unit
) {
    val context = LocalContext.current
    val userRepository = remember {
        UserRepository(
            FirebaseAuth.getInstance(),
            FirebaseFirestore.getInstance(),
            AppDatabase.getInstance(context)
        )
    }

    var user by remember { mutableStateOf<User?>(null) }
    var workouts by remember { mutableStateOf<List<Workout>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val stepGoal by settingsViewModel.stepGoal.collectAsState()

    LaunchedEffect(userEmail) {
        when (val result = userRepository.getUserData(userEmail)) {
            is Result.Success -> user = result.data
            is Result.Error -> {}
        }
        workouts = userRepository.getWorkouts(userEmail)
        isLoading = false
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Profile", fontFamily = KdamThmorPro) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            MountainHeaderFullScreen(modifier = Modifier.matchParentSize())

            // ← outer Column starts here
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(top = 16.dp)
            ) {
                // ← inner Column for user info
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

                    val todaySteps = remember(workouts, stepGoal) {
                        val today = Calendar.getInstance()
                        workouts.filter { w ->
                            val cal = Calendar.getInstance().apply { time = w.timestamp.toDate() }
                            cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                                    cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
                        }.sumOf { it.steps }
                    }
                    val progress = if (stepGoal > 0) todaySteps / stepGoal.toFloat() else 0f

                    // ← nested Column for progress bar
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, start = 24.dp, end = 24.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = progress.coerceIn(0f, 1f),
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFFE37028)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "$todaySteps / $stepGoal steps",
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = KdamThmorPro
                        )
                    }
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
            } // ← outer Column ends here
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
            Text(
                "Date: ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(workout.timestamp.toDate())}"
            )
            Text("Distance: ${"%.2f".format(workout.distanceMeters / 1000)} km")
            Text("Duration: ${formatDuration(workout.durationMs)}")
        }
    }
}
