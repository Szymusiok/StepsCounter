// SettingsScreen.kt
package eu.tutorials.stepscounter.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    // Collect state
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val activityRecognitionEnabled by viewModel.activityRecognitionEnabled.collectAsState()
    val distanceUnit by viewModel.distanceUnit.collectAsState()
    val appTheme by viewModel.appTheme.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = {
                        onBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // — Notifications —
            ListItem(
                headlineContent = { Text("Enable Notifications") },
                supportingContent = { Text("Receive milestone & reminder alerts") },
                trailingContent = {
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = viewModel::setNotificationsEnabled
                    )
                }
            )
            Divider()

            // — Activity Recognition —
            ListItem(
                headlineContent = { Text("Activity Recognition") },
                supportingContent = { Text("Allow step counting & motion detection") },
                trailingContent = {
                    Switch(
                        checked = activityRecognitionEnabled,
                        onCheckedChange = viewModel::setActivityRecognitionEnabled
                    )
                }
            )
            Divider()

            // — Units Section —
            SectionHeader(text = "Units")
            ListItem(
                headlineContent = { Text("Kilometers") },
                trailingContent = {
                    RadioButton(
                        selected = distanceUnit == SettingsViewModel.DistanceUnit.KILOMETERS,
                        onClick = { viewModel.setDistanceUnit(SettingsViewModel.DistanceUnit.KILOMETERS) }
                    )
                },
                modifier = Modifier.clickable {
                    viewModel.setDistanceUnit(SettingsViewModel.DistanceUnit.KILOMETERS)
                }
            )
            ListItem(
                headlineContent = { Text("Miles") },
                trailingContent = {
                    RadioButton(
                        selected = distanceUnit == SettingsViewModel.DistanceUnit.MILES,
                        onClick = { viewModel.setDistanceUnit(SettingsViewModel.DistanceUnit.MILES) }
                    )
                },
                modifier = Modifier.clickable {
                    viewModel.setDistanceUnit(SettingsViewModel.DistanceUnit.MILES)
                }
            )
            Divider()

            // — Theme Section —
            SectionHeader(text = "Theme")
            SettingsViewModel.AppTheme.values().forEach { theme ->
                val label = when (theme) {
                    SettingsViewModel.AppTheme.LIGHT  -> "Light"
                    SettingsViewModel.AppTheme.DARK   -> "Dark"
                    SettingsViewModel.AppTheme.SYSTEM -> "Follow System"
                }
                ListItem(
                    headlineContent = { Text(label) },
                    trailingContent = {
                        RadioButton(
                            selected = appTheme == theme,
                            onClick = { viewModel.setAppTheme(theme) }
                        )
                    },
                    modifier = Modifier.clickable { viewModel.setAppTheme(theme) }
                )
            }
            Divider()

            // — Profile & About —
            ListItem(
                headlineContent = { Text("Profile") },
                supportingContent = { Text("View or edit your profile") },
                trailingContent = {
                    Icon(Icons.Default.Person, contentDescription = "Profile")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { /* TODO: Profile screen */ }
            )
            Divider()
            ListItem(
                headlineContent = { Text("About") },
                supportingContent = { Text("App version, licenses, etc.") },
                trailingContent = {
                    Icon(Icons.Default.Info, contentDescription = "About")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { /* TODO: show About dialog */ }
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
    )
}
