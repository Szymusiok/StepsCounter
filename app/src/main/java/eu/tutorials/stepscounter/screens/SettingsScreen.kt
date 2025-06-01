package eu.tutorials.stepscounter.screens.settings

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
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
import android.Manifest
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import eu.tutorials.stepscounter.KdamThmorPro
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onProfile: () -> Unit,
    onAbout: () -> Unit,
    viewModel: SettingsViewModel
) {
    val scope = rememberCoroutineScope()
    val snackHost = remember { SnackbarHostState() }

    val context = LocalContext.current
    val activityRecognitionEnabled by viewModel.activityRecognitionEnabled.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val distanceUnit by viewModel.distanceUnit.collectAsState()
    val appTheme by viewModel.appTheme.collectAsState()
    val sensorType by viewModel.sensorType.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = {
            viewModel.setActivityRecognitionEnabled(it)
            scope.launch {
                snackHost.showSnackbar(if (it) "Activity recognition enabled" else "Permission denied")
            }
        }
    )

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
        containerColor = background,
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = textColor, fontFamily = KdamThmorPro) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = textColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackHost) }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            MountainHeader()

            Column(
                Modifier
                    .fillMaxWidth()
                    .background(background)
                    .padding(horizontal = 24.dp)
                    .padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                SettingToggle("Activity Recognition", activityRecognitionEnabled) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        permissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                    } else viewModel.setActivityRecognitionEnabled(it)
                }

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
                    textColor = textColor,
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

                SettingSegment(
                    title = "Theme",
                    options = listOf("Light", "Dark", "System"),
                    selectedIndex = when (appTheme) {
                        SettingsViewModel.AppTheme.LIGHT -> 0
                        SettingsViewModel.AppTheme.DARK -> 1
                        SettingsViewModel.AppTheme.SYSTEM -> 2
                    },
                    onSelectIndex = {
                        viewModel.setAppTheme(
                            when (it) {
                                0 -> SettingsViewModel.AppTheme.LIGHT
                                1 -> SettingsViewModel.AppTheme.DARK
                                else -> SettingsViewModel.AppTheme.SYSTEM
                            }
                        )
                    },
                    accent = accent,
                    controlColor = surface,
                    textColor = textColor
                )

                TextButton(
                    onClick = onAbout,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("About", color = accent, textAlign = TextAlign.Center, fontFamily = KdamThmorPro)
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun MountainHeader(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxWidth().height(120.dp)) {
        val width = size.width
        val height = size.height

        val back = Path().apply {
            moveTo(0f, height)
            lineTo(0f, height * 0.5f)
            cubicTo(width * 0.25f, 0f, width * 0.75f, height, width, height * 0.4f)
            lineTo(width, height)
            close()
        }

        val front = Path().apply {
            moveTo(0f, height)
            lineTo(0f, height * 0.65f)
            cubicTo(width * 0.3f, height * 0.3f, width * 0.7f, height * 0.9f, width, height * 0.5f)
            lineTo(width, height)
            close()
        }

        drawPath(back, Color(0xFFEDE9E0))
        drawPath(front, Color(0xFFDBD5C7))
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
        Switch(checked = value, onCheckedChange = onToggle,colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = Color(0xFFE37028),
            uncheckedThumbColor = Color.White,
            uncheckedTrackColor = Color.LightGray
        ))
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
        Text(title, style = MaterialTheme.typography.bodyLarge, color = textColor, fontFamily = KdamThmorPro)
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(controlColor),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            options.forEachIndexed { i, label ->
                val selected = i == selectedIndex
                val background = if (selected) accent else Color.Transparent
                val contentColor = if (selected) Color.White else textColor

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectIndex(i) }
                        .background(background, RoundedCornerShape(24.dp))
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(label, color = contentColor, style = MaterialTheme.typography.labelLarge, fontFamily = KdamThmorPro)
                }
            }
        }
    }
}
