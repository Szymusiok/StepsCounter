// SettingsViewModel.kt
package eu.tutorials.stepscounter.screens.settings

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = app.getSharedPreferences("user_settings", Context.MODE_PRIVATE)

    // --- Notifications toggle ---
    private val _notificationsEnabled = MutableStateFlow(
        prefs.getBoolean("notifications_enabled", true)
    )
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    fun setNotificationsEnabled(on: Boolean) {
        prefs.edit().putBoolean("notifications_enabled", on).apply()
        _notificationsEnabled.value = on
    }

    // --- Activity Recognition toggle ---
    private val _activityRecognitionEnabled = MutableStateFlow(
        prefs.getBoolean("activity_recognition_enabled", true)
    )
    val activityRecognitionEnabled: StateFlow<Boolean> = _activityRecognitionEnabled.asStateFlow()

    fun setActivityRecognitionEnabled(on: Boolean) {
        prefs.edit().putBoolean("activity_recognition_enabled", on).apply()
        _activityRecognitionEnabled.value = on
    }

    // --- Distance units ---
    enum class DistanceUnit { KILOMETERS, MILES }
    private val _distanceUnit = MutableStateFlow(
        DistanceUnit.valueOf(
            prefs.getString("distance_unit", DistanceUnit.KILOMETERS.name)!!
        )
    )
    val distanceUnit: StateFlow<DistanceUnit> = _distanceUnit.asStateFlow()

    fun setDistanceUnit(unit: DistanceUnit) {
        prefs.edit().putString("distance_unit", unit.name).apply()
        _distanceUnit.value = unit
    }

    // --- App theme ---
    enum class AppTheme { LIGHT, DARK, SYSTEM }
    private val _appTheme = MutableStateFlow(
        AppTheme.valueOf(
            prefs.getString("app_theme", AppTheme.SYSTEM.name)!!
        )
    )
    val appTheme: StateFlow<AppTheme> = _appTheme.asStateFlow()

    fun setAppTheme(theme: AppTheme) {
        prefs.edit().putString("app_theme", theme.name).apply()
        _appTheme.value = theme

        // apply immediately
        when (theme) {
            AppTheme.LIGHT  -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            AppTheme.DARK   -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            AppTheme.SYSTEM -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }
}
