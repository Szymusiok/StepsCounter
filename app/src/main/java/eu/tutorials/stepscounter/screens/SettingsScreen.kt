package eu.tutorials.stepscounter.screens.settings

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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

    // collect state
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val activityRecognitionEnabled by viewModel.activityRecognitionEnabled.collectAsState()
    val distanceUnit by viewModel.distanceUnit.collectAsState()
    val appTheme by viewModel.appTheme.collectAsState()

    // permission launcher
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            viewModel.setActivityRecognitionEnabled(granted)
            scope.launch {
                snackHost.showSnackbar(
                    if (granted) "Activity recognition enabled"
                    else "Permission denied, step counting disabled"
                )
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackHost) }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // Notifications
            SettingsToggleItem(
                label = "Enable Notifications",
                description = "Receive milestone & reminder alerts",
                checked = notificationsEnabled,
                onCheckedChange = {
                    viewModel.setNotificationsEnabled(it)
                    scope.launch {
                        snackHost.showSnackbar(
                            if (it) "Notifications enabled"
                            else "Notifications disabled"
                        )
                    }
                }
            )

            Divider(Modifier.padding(vertical = 8.dp))

            // Activity recognition toggle
            SettingsToggleItem(
                label = "Activity Recognition",
                description = "Allow step counting & motion detection",
                checked = activityRecognitionEnabled,
                onCheckedChange = { enabled ->
                    // request permission on Android Q+
                    if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        permissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                    } else {
                        viewModel.setActivityRecognitionEnabled(enabled)
                    }
                }
            )

            Divider(Modifier.padding(vertical = 8.dp))

            // Units
            SectionHeader(text = "Units")
            SettingsRadioItem(
                label = "Kilometers",
                selected = distanceUnit == SettingsViewModel.DistanceUnit.KILOMETERS,
                onSelect = { viewModel.setDistanceUnit(SettingsViewModel.DistanceUnit.KILOMETERS) }
            )
            SettingsRadioItem(
                label = "Miles",
                selected = distanceUnit == SettingsViewModel.DistanceUnit.MILES,
                onSelect = { viewModel.setDistanceUnit(SettingsViewModel.DistanceUnit.MILES) }
            )

            Divider(Modifier.padding(vertical = 8.dp))

            // Theme
            SectionHeader(text = "Theme")
            SettingsViewModel.AppTheme.values().forEach { theme ->
                val label = when (theme) {
                    SettingsViewModel.AppTheme.LIGHT  -> "Light"
                    SettingsViewModel.AppTheme.DARK   -> "Dark"
                    SettingsViewModel.AppTheme.SYSTEM -> "Follow System"
                }
                SettingsRadioItem(
                    label = label,
                    selected = appTheme == theme,
                    onSelect = { viewModel.setAppTheme(theme) }
                )
            }

            Divider(Modifier.padding(vertical = 8.dp))

            // Profile
            ListItem(
                headlineContent = { Text("Profile") },
                supportingContent = { Text("View or edit your profile") },
                trailingContent = {
                    Icon(Icons.Default.Person, contentDescription = "Profile")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onProfile() }
                    .padding(vertical = 8.dp)
            )

            Divider()

            // About
            ListItem(
                headlineContent = { Text("About") },
                supportingContent = { Text("App version, licenses, etc.") },
                trailingContent = {
                    Icon(Icons.Default.Info, contentDescription = "About")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAbout() }
                    .padding(vertical = 8.dp)
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SettingsToggleItem(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = { Text(label) },
        supportingContent = { Text(description) },
        trailingContent = {
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        },
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
private fun SettingsRadioItem(
    label: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    ListItem(
        headlineContent = { Text(label) },
        trailingContent = {
            RadioButton(selected = selected, onClick = onSelect)
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .padding(vertical = 4.dp)
    )
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .padding(top = 16.dp, bottom = 8.dp)
    )
}
