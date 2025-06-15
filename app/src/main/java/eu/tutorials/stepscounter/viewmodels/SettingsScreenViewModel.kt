package eu.tutorials.stepscounter.viewmodels

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

open class SettingsViewModel(app: Application) : AndroidViewModel(app) {
    private val prefs = app.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val sensorManager = app.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    companion object {
        private const val PREFS_NAME = "user_settings"
        private const val KEY_NOTIF = "notifications_enabled"
        private const val KEY_ACTIVITY = "activity_recognition_enabled"
        private const val KEY_UNIT = "distance_unit"
        private const val KEY_THEME = "app_theme"
        private const val KEY_SENSOR = "sensor_type"
        private const val KEY_GOAL = "daily_step_goal"
    }

    enum class DistanceUnit { KILOMETERS, MILES }
    enum class AppTheme { LIGHT, DARK, SYSTEM }
    enum class SensorType { STEP_SENSOR, ACCELEROMETER }

    // Notifications
    private val _notificationsEnabled = MutableStateFlow(prefs.getBoolean(KEY_NOTIF, true))
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()
    fun setNotificationsEnabled(on: Boolean) = updatePref(KEY_NOTIF, on) {
        _notificationsEnabled.value = on
    }

    // Activity recognition
    private val _activityRecognitionEnabled = MutableStateFlow(prefs.getBoolean(KEY_ACTIVITY, true))
    val activityRecognitionEnabled: StateFlow<Boolean> = _activityRecognitionEnabled.asStateFlow()
    fun setActivityRecognitionEnabled(on: Boolean) = updatePref(KEY_ACTIVITY, on) {
        _activityRecognitionEnabled.value = on
    }

    // Distance units
    private val _distanceUnit = MutableStateFlow(
        DistanceUnit.valueOf(prefs.getString(KEY_UNIT, DistanceUnit.KILOMETERS.name)!!)
    )
    val distanceUnit: StateFlow<DistanceUnit> = _distanceUnit.asStateFlow()
    fun setDistanceUnit(unit: DistanceUnit) = updatePref(KEY_UNIT, unit.name) {
        _distanceUnit.value = unit
    }

    // App theme
    private val _appTheme = MutableStateFlow(
        AppTheme.valueOf(prefs.getString(KEY_THEME, AppTheme.SYSTEM.name)!!)
    )
    val appTheme: StateFlow<AppTheme> = _appTheme.asStateFlow()
    fun setAppTheme(theme: AppTheme) {
        updatePref(KEY_THEME, theme.name) { _appTheme.value = theme }
        AppCompatDelegate.setDefaultNightMode(
            when (theme) {
                AppTheme.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
                AppTheme.DARK -> AppCompatDelegate.MODE_NIGHT_YES
                AppTheme.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
        )
    }

    // Daily step goal
    private val _stepGoal = MutableStateFlow(prefs.getInt(KEY_GOAL, 10000))
    val stepGoal: StateFlow<Int> = _stepGoal.asStateFlow()
    fun setStepGoal(goal: Int) = updatePref(KEY_GOAL, goal) {
        _stepGoal.value = goal
    }

    // Sensor type (step or accelerometer)
    private val _sensorType = MutableStateFlow(
        SensorType.valueOf(prefs.getString(KEY_SENSOR, SensorType.ACCELEROMETER.name)!!)
    )
    val sensorType: StateFlow<SensorType> = _sensorType.asStateFlow()

    private val _sensorError = MutableSharedFlow<String>()
    val sensorError: SharedFlow<String> = _sensorError.asSharedFlow()

    fun setSensorType(type: SensorType) {
        if (type == SensorType.STEP_SENSOR && !hasStepSensor()) {
            viewModelScope.launch {
                _sensorError.emit("Step counter sensor not available. Reverting to Accelerometer.")
            }
            updatePref(KEY_SENSOR, SensorType.ACCELEROMETER.name) {
                _sensorType.value = SensorType.ACCELEROMETER
            }
        } else {
            updatePref(KEY_SENSOR, type.name) {
                _sensorType.value = type
            }
        }
    }

    private fun hasStepSensor(): Boolean {
        return sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null
    }

    private fun <T> updatePref(key: String, value: T, after: () -> Unit) {
        viewModelScope.launch {
            prefs.edit().apply {
                when (value) {
                    is Boolean -> putBoolean(key, value)
                    is String -> putString(key, value)
                    is Int -> putInt(key, value)
                    else -> error("Unsupported type")
                }
                apply()
            }
            after()
        }
    }
}
