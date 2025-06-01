// StepsViewModel.kt
package eu.tutorials.stepscounter.viewmodels

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs
import kotlin.math.sqrt

class StepsViewModel(application: Application) : AndroidViewModel(application),
    SensorEventListener {

    private val sensorManager =
        application.getSystemService(Application.SENSOR_SERVICE) as SensorManager

    // Placeholder for sensors; we'll retrieve fresh in startTracking()
    private var stepSensor: Sensor? = null
    private var accelSensor: Sensor? = null

    // one‐time fallback event
    private val _fallbackToAccel = MutableLiveData(false)
    val fallbackToAccel: LiveData<Boolean> = _fallbackToAccel

    private val _steps = MutableStateFlow(0)
    val steps: StateFlow<Int> = _steps.asStateFlow()

    // for step‐counter
    private var initialCount: Int? = null

    // for accel fallback (high‐pass + EMA)
    private val prevAccel   = FloatArray(3)
    private val hpFiltered  = FloatArray(3)
    private val hpAlpha     = 0.96f
    private var avgMag      = 0f
    private var meanDev     = 0f
    private val emaAlpha    = 0.9f
    private val thresholdF  = 2.5f
    private var prevMag     = 0f
    private val minStepIntv = 300L
    private var lastStepTs  = 0L

    private var isUsingStepSensor = false
    private var isTracking        = false

    fun startTracking() {
        if (isTracking) return

        // reset state
        _steps.value     = 0
        initialCount     = null
        lastStepTs       = 0L
        avgMag           = 0f
        meanDev          = 0f
        prevMag          = 0f
        prevAccel.fill(0f)
        hpFiltered.fill(0f)

        // detect sensors and permissions here
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        val hasPermission = ContextCompat.checkSelfPermission(
            getApplication(),
            Manifest.permission.ACTIVITY_RECOGNITION
        ) == PackageManager.PERMISSION_GRANTED

        if (stepSensor != null && hasPermission) {
            // use hardware step counter
            isUsingStepSensor = true
            sensorManager.registerListener(
                this,
                stepSensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        } else if (accelSensor != null) {
            // fallback to accelerometer
            isUsingStepSensor = false
            _fallbackToAccel.value = true
            sensorManager.registerListener(
                this,
                accelSensor,
                SensorManager.SENSOR_DELAY_GAME
            )
        }

        isTracking = true
    }

    fun stopTracking() {
        if (!isTracking) return
        sensorManager.unregisterListener(this)
        isTracking = false
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (isUsingStepSensor && event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
            val total = event.values.getOrNull(0)?.toInt() ?: return
            if (initialCount == null) initialCount = total
            _steps.value = (total - (initialCount ?: total)).coerceAtLeast(0)

        } else if (!isUsingStepSensor && event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            // high-pass gravity removal
            for (i in 0..2) {
                hpFiltered[i] = hpAlpha * (hpFiltered[i] + event.values[i] - prevAccel[i])
                prevAccel[i]   = event.values[i]
            }
            // magnitude
            val mag = sqrt(
                hpFiltered[0]*hpFiltered[0] +
                        hpFiltered[1]*hpFiltered[1] +
                        hpFiltered[2]*hpFiltered[2]
            )
            // EMA threshold
            avgMag  = emaAlpha * avgMag  + (1 - emaAlpha) * mag
            meanDev = emaAlpha * meanDev + (1 - emaAlpha) * abs(mag - avgMag)
            val thr = avgMag + thresholdF * meanDev

            val now = System.currentTimeMillis()
            if (mag > thr && prevMag <= thr && now - lastStepTs > minStepIntv) {
                _steps.value = _steps.value + 1
                lastStepTs   = now
            }
            prevMag = mag
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { /* no-op */ }

    override fun onCleared() {
        super.onCleared()
        sensorManager.unregisterListener(this)
    }
}