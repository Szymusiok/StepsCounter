// SettingsViewModel.kt
package eu.tutorials.stepscounter.screens.settings

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel : ViewModel() {

    // Notifications toggle
    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled

    // Activity recognition toggle
    private val _activityRecognitionEnabled = MutableStateFlow(true)
    val activityRecognitionEnabled: StateFlow<Boolean> = _activityRecognitionEnabled

    // Distance units
    enum class DistanceUnit { KILOMETERS, MILES }
    private val _distanceUnit = MutableStateFlow(DistanceUnit.KILOMETERS)
    val distanceUnit: StateFlow<DistanceUnit> = _distanceUnit

    // App theme
    enum class AppTheme { LIGHT, DARK, SYSTEM }
    private val _appTheme = MutableStateFlow(AppTheme.SYSTEM)
    val appTheme: StateFlow<AppTheme> = _appTheme

    fun setNotificationsEnabled(on: Boolean) {
        _notificationsEnabled.value = on
        // TODO: persist to DataStore
    }

    fun setActivityRecognitionEnabled(on: Boolean) {
        _activityRecognitionEnabled.value = on
        // TODO: persist to DataStore
    }

    fun setDistanceUnit(unit: DistanceUnit) {
        _distanceUnit.value = unit
        // TODO: persist to DataStore
    }

    fun setAppTheme(theme: AppTheme) {
        _appTheme.value = theme
        // TODO: apply/change theme in your AppCompatDelegate or Compose
    }
}
