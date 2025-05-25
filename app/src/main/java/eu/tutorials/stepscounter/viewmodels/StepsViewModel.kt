package eu.tutorials.stepscounter.viewmodels

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


class StepsViewModel(application: Application) : AndroidViewModel(application),
    SensorEventListener {

    private val sensorManager =
        application.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    // Use the STEP_COUNTER sensor (cumulative total since boot)
    private val stepSensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    // backing StateFlow
    private val _steps = MutableStateFlow(0)
    val steps: StateFlow<Int> = _steps.asStateFlow()

    private var initialCount: Int? = null
    private var isTracking = false

    /** Call when you want to begin counting steps */
    fun startTracking() {
        if (isTracking || stepSensor == null) return

        // reset both displayed count and baseline
        _steps.value = 0
        initialCount = null

        sensorManager.registerListener(
            this,
            stepSensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )
        isTracking = true
    }

    /** Call to stop counting steps */
    fun stopTracking() {
        if (!isTracking) return

        sensorManager.unregisterListener(this)
        isTracking = false
        initialCount = null
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_STEP_COUNTER) return

        // event.values[0] is the total steps since device boot
        val totalSinceBoot = event.values.getOrNull(0)?.toInt() ?: return

        // set baseline at first reading
        if (initialCount == null) {
            initialCount = totalSinceBoot
        }

        // current session steps = totalSinceBoot - baseline
        val sessionSteps = totalSinceBoot - (initialCount ?: totalSinceBoot)

        // ensure non-negative and update StateFlow
        _steps.value = sessionSteps.coerceAtLeast(0)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // no-op
    }

    override fun onCleared() {
        super.onCleared()
        // clean up in case someone forgets to call stopTracking()
        sensorManager.unregisterListener(this)
    }
}
