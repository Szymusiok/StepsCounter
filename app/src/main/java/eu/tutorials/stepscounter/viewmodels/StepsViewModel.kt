package eu.tutorials.stepscounter.viewmodels

import android.app.Application
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs
import kotlin.math.sqrt

open class StepsViewModel(private val app: Application) : AndroidViewModel(app), SensorEventListener {

    private val sensorManager = app.getSystemService(Application.SENSOR_SERVICE) as SensorManager

    private var stepSensor: Sensor? = null
    private var accelSensor: Sensor? = null

    private val _steps = MutableStateFlow(0)
    val steps: StateFlow<Int> = _steps.asStateFlow()

    private var initialCount: Int? = null
    private val prevAccel = FloatArray(3)
    private val hpFiltered = FloatArray(3)
    private val hpAlpha = 0.96f
    private var avgMag = 0f
    private var meanDev = 0f
    private val emaAlpha = 0.9f
    private val thresholdF = 2.5f
    private var prevMag = 0f
    private val minStepIntv = 300L
    private var lastStepTs = 0L

    private var isUsingStepSensor = false
    private var isTracking = false
    private var sensorTypeCached: SettingsViewModel.SensorType? = null

    fun startTracking() {
        if (isTracking) return
        resetState()

        val sensorType = SettingsViewModel.SensorType.valueOf(
            app.getSharedPreferences("user_settings", 0).getString("sensor_type", SettingsViewModel.SensorType.STEP_SENSOR.name)!!
        )
        sensorTypeCached = sensorType

        if (sensorType == SettingsViewModel.SensorType.STEP_SENSOR) {
            stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
            if (stepSensor != null) {
                isUsingStepSensor = true
                sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL)
            }
        }

        if (!isUsingStepSensor) {
            accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            accelSensor?.let {
                isUsingStepSensor = false
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
            }
        }

        isTracking = true
    }

    fun stopTracking() {
        if (!isTracking) return
        sensorManager.unregisterListener(this)
        isTracking = false
    }

    fun resumeTracking() {
        if (isTracking) return

        val sensorType = sensorTypeCached ?: SettingsViewModel.SensorType.valueOf(
            app.getSharedPreferences("user_settings", 0).getString("sensor_type", SettingsViewModel.SensorType.STEP_SENSOR.name)!!
        )
        sensorTypeCached = sensorType

        if (sensorType == SettingsViewModel.SensorType.STEP_SENSOR) {
            stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
            if (stepSensor != null) {
                isUsingStepSensor = true
                sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL)
            }
        }

        if (!isUsingStepSensor) {
            accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            accelSensor?.let {
                isUsingStepSensor = false
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
            }
        }

        isTracking = true
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (isUsingStepSensor && event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
            val total = event.values.getOrNull(0)?.toInt() ?: return
            if (initialCount == null) initialCount = total
            _steps.value = (total - (initialCount ?: total)).coerceAtLeast(0)
        } else if (!isUsingStepSensor && event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            for (i in 0..2) {
                hpFiltered[i] = hpAlpha * (hpFiltered[i] + event.values[i] - prevAccel[i])
                prevAccel[i] = event.values[i]
            }
            val mag = sqrt(hpFiltered[0] * hpFiltered[0] + hpFiltered[1] * hpFiltered[1] + hpFiltered[2] * hpFiltered[2])
            avgMag = emaAlpha * avgMag + (1 - emaAlpha) * mag
            meanDev = emaAlpha * meanDev + (1 - emaAlpha) * abs(mag - avgMag)
            val thr = avgMag + thresholdF * meanDev

            val now = System.currentTimeMillis()
            if (mag > thr && prevMag <= thr && now - lastStepTs > minStepIntv) {
                _steps.value = _steps.value + 1
                lastStepTs = now
            }
            prevMag = mag
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onCleared() {
        super.onCleared()
        stopTracking()
    }

    private fun resetState() {
        _steps.value = 0
        initialCount = null
        lastStepTs = 0L
        avgMag = 0f
        meanDev = 0f
        prevMag = 0f
        prevAccel.fill(0f)
        hpFiltered.fill(0f)
    }
}
