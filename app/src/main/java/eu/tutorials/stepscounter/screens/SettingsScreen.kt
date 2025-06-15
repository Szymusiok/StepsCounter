package eu.tutorials.stepscounter.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import eu.tutorials.stepscounter.ui.theme.KdamThmorPro
import eu.tutorials.stepscounter.MainActivity
import eu.tutorials.stepscounter.utils.MountainHeaderFullScreen
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color
import eu.tutorials.stepscounter.viewmodels.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    viewModel: SettingsViewModel
) {
    val context = LocalContext.current

    val scope = rememberCoroutineScope()
    val snackHost = remember { SnackbarHostState() }

    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val distanceUnit by viewModel.distanceUnit.collectAsState()
    val appTheme by viewModel.appTheme.collectAsState()
    val sensorType by viewModel.sensorType.collectAsState()
    val stepGoal by viewModel.stepGoal.collectAsState()

    var shouldLogout by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = {
            viewModel.setActivityRecognitionEnabled(it)
            scope.launch {
                snackHost.showSnackbar(if (it) "Activity recognition enabled" else "Permission denied")
            }
        }
    )

    if (shouldLogout) {
        LaunchedEffect(Unit) {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            context.startActivity(intent)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.sensorError.collect {
            snackHost.showSnackbar(it)
        }
    }

    val background = Color(0xFFFFFBF6)
    val accent = Color(0xFFE37028)
    val surface = Color(0xFFF3EFEA)
    val textColor = Color(0xFF222222)

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = textColor, fontFamily = KdamThmorPro) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = textColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackHost) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            MountainHeaderFullScreen(modifier = Modifier.matchParentSize()) // Background
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {

                    SettingToggle("Enable Notifications", notificationsEnabled) {
                        viewModel.setNotificationsEnabled(it)
                    }

                    SettingSegment(
                        title = "Units",
                        options = listOf("Kilometers", "Miles"),
                        selectedIndex = if (distanceUnit == SettingsViewModel.DistanceUnit.KILOMETERS) 0 else 1,
                        onSelectIndex = {
                            viewModel.setDistanceUnit(
                                if (it == 0) SettingsViewModel.DistanceUnit.KILOMETERS else SettingsViewModel.DistanceUnit.MILES
                            )
                        },
                        accent = accent,
                        controlColor = surface,
                        textColor = textColor
                    )

                    SettingSegment(
                        title = "Sensor Source",
                        options = listOf("Step Sensor", "Accelerometer"),
                        selectedIndex = if (sensorType == SettingsViewModel.SensorType.STEP_SENSOR) 0 else 1,
                        onSelectIndex = {
                            viewModel.setSensorType(
                                if (it == 0) SettingsViewModel.SensorType.STEP_SENSOR else SettingsViewModel.SensorType.ACCELEROMETER
                            )
                        },
                        accent = accent,
                        controlColor = surface,
                        textColor = textColor
                    )

                    SettingSlider(
                        title = "Daily Step Goal",
                        value = stepGoal.toFloat(),
                        onValueChange = { viewModel.setStepGoal(it.toInt()) },
                        valueRange = 1000f..20000f,
                        steps = 18,
                        accent = accent,
                        textColor = textColor
                    )

                    Button(
                        onClick = {
                            FirebaseAuth.getInstance().signOut()
                            shouldLogout = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("LOGOUT", color = MaterialTheme.colorScheme.onError)
                    }

                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}



@Composable
fun SettingToggle(title: String, value: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, style = MaterialTheme.typography.bodyLarge, fontFamily = KdamThmorPro)
        Switch(
            checked = value,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFFE37028),
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color.LightGray
            )
        )
    }
}

@Composable
fun SettingSegment(
    title: String,
    options: List<String>,
    selectedIndex: Int,
    onSelectIndex: (Int) -> Unit,
    accent: Color,
    controlColor: Color,
    textColor: Color
) {
    Column {
        Text(
            title,
            style = MaterialTheme.typography.bodyLarge,
            color = textColor,
            fontFamily = KdamThmorPro,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(6.dp, shape = RoundedCornerShape(32.dp))
                .clip(RoundedCornerShape(32.dp)),
            color = controlColor
        ) {
            Row(
                Modifier
                    .background(controlColor)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                options.forEachIndexed { i, label ->
                    val selected = i == selectedIndex
                    Surface(
                        color = if (selected) accent else controlColor,
                        shape = RoundedCornerShape(28.dp),
                        tonalElevation = if (selected) 4.dp else 0.dp,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                            .clickable { onSelectIndex(i) }
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .padding(vertical = 12.dp)
                        ) {
                            Text(
                                label,
                                fontFamily = KdamThmorPro,
                                color = if (selected) Color.White else textColor.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingSlider(
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    accent: Color,
    textColor: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = textColor, fontFamily = KdamThmorPro)
            Text(value.toInt().toString(), style = MaterialTheme.typography.bodyMedium, color = textColor, fontFamily = KdamThmorPro)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor = accent,
                activeTrackColor = accent
            )
        )
    }
}
