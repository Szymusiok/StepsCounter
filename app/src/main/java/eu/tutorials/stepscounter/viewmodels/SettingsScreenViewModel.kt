package eu.tutorials.stepscounter.screens.settings

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsViewModel(app: Application) : AndroidViewModel(app) {
    private val prefs = app.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "user_settings"
        private const val KEY_NOTIF = "notifications_enabled"
        private const val KEY_ACTIVITY = "activity_recognition_enabled"
        private const val KEY_UNIT = "distance_unit"
        private const val KEY_THEME = "app_theme"
    }

    // Notifications
    private val _notificationsEnabled = MutableStateFlow(
        prefs.getBoolean(KEY_NOTIF, true)
    )
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()
    fun setNotificationsEnabled(on: Boolean) = updatePref(KEY_NOTIF, on) {
        _notificationsEnabled.value = on
    }

    // Activity recognition
    private val _activityRecognitionEnabled = MutableStateFlow(
        prefs.getBoolean(KEY_ACTIVITY, true)
    )
    val activityRecognitionEnabled: StateFlow<Boolean> = _activityRecognitionEnabled.asStateFlow()
    fun setActivityRecognitionEnabled(on: Boolean) = updatePref(KEY_ACTIVITY, on) {
        _activityRecognitionEnabled.value = on
    }

    // Distance units
    enum class DistanceUnit { KILOMETERS, MILES }
    private val _distanceUnit = MutableStateFlow(
        DistanceUnit.valueOf(prefs.getString(KEY_UNIT, DistanceUnit.KILOMETERS.name)!!)
    )
    val distanceUnit: StateFlow<DistanceUnit> = _distanceUnit.asStateFlow()
    fun setDistanceUnit(unit: DistanceUnit) = updatePref(KEY_UNIT, unit.name) {
        _distanceUnit.value = unit
    }

    // App theme
    enum class AppTheme { LIGHT, DARK, SYSTEM }
    private val _appTheme = MutableStateFlow(
        AppTheme.valueOf(prefs.getString(KEY_THEME, AppTheme.SYSTEM.name)!!)
    )
    val appTheme: StateFlow<AppTheme> = _appTheme.asStateFlow()
    fun setAppTheme(theme: AppTheme) {
        updatePref(KEY_THEME, theme.name) { _appTheme.value = theme }
        AppCompatDelegate.setDefaultNightMode(
            when (theme) {
                AppTheme.LIGHT  -> AppCompatDelegate.MODE_NIGHT_NO
                AppTheme.DARK   -> AppCompatDelegate.MODE_NIGHT_YES
                AppTheme.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
        )
    }

    // helper to write pref + callback
    private fun <T> updatePref(key: String, value: T, after: () -> Unit) {
        viewModelScope.launch {
            prefs.edit().apply {
                when (value) {
                    is Boolean -> putBoolean(key, value)
                    is String  -> putString(key, value)
                    else       -> error("Unsupported type")
                }
                apply()
            }
            after()
        }
    }
}
