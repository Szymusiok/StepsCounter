package eu.tutorials.stepscounter.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import eu.tutorials.stepscounter.LocationTracker
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TrackingViewModel(
    ctx: Context,
    private val stepsViewModel: StepsViewModel
) : ViewModel() {

    private val tracker = LocationTracker(ctx)

    private val _isTracking    = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    private val _pathPoints    = MutableStateFlow<List<LatLng>>(emptyList())
    val pathPoints: StateFlow<List<LatLng>> = _pathPoints.asStateFlow()

    private val _totalDistance = MutableStateFlow(0.0)
    val totalDistance: StateFlow<Double> = _totalDistance.asStateFlow()

    private val _calories      = MutableStateFlow(0.0)
    val calories: StateFlow<Double> = _calories.asStateFlow()

    private val _elapsedTime   = MutableStateFlow(0L)
    val elapsedTime: StateFlow<Long> = _elapsedTime.asStateFlow()

    private val _speed         = MutableStateFlow(0.0)
    val speed: StateFlow<Double> = _speed.asStateFlow()

    private val _pace          = MutableStateFlow(0.0)
    val pace: StateFlow<Double> = _pace.asStateFlow()

    private var locationJob: Job? = null
    private var timerJob:    Job? = null

    private fun updateMetrics() {
        val hours = _elapsedTime.value / 3_600_000.0
        _speed.value = if (hours > 0) (_totalDistance.value / 1000.0) / hours else 0.0

        val distKm = _totalDistance.value / 1000.0
        _pace.value = if (distKm > 0) (_elapsedTime.value / 60_000.0) / distKm else 0.0
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_isTracking.value && !_isPaused.value) {
                delay(1000L)
                _elapsedTime.update { it + 1000L }
                updateMetrics()
            }
        }
    }

    private fun startLocationUpdates() {
        locationJob?.cancel()
        locationJob = viewModelScope.launch {
            tracker.locationUpdates().collect { newPoint ->
                if (_isPaused.value || !_isTracking.value) return@collect

                val prev = _pathPoints.value.lastOrNull()
                _pathPoints.update { old -> old + newPoint }

                prev?.let {
                    val delta = SphericalUtil.computeDistanceBetween(it, newPoint)
                    _totalDistance.update { old -> old + delta }
                }

                _calories.value = (_totalDistance.value / 1000.0) * 60.0
                updateMetrics()
            }
        }
    }

    fun startTracking() {
        if (_isTracking.value) return

        viewModelScope.launch {
            val startLocation = tracker.getCurrentLocation()
            if (startLocation == null) return@launch // don't start without valid location

            val startPoint = LatLng(startLocation.latitude, startLocation.longitude)

            // reset state
            _pathPoints.value    = listOf(startPoint)
            _totalDistance.value = 0.0
            _calories.value      = 0.0
            _speed.value = 0.0
            _pace.value = 0.0
            _isPaused.value = false
            _elapsedTime.value   = 0L
            _isTracking.value    = true

            startTimer()
            startLocationUpdates()

            stepsViewModel.startTracking()
        }
    }

    fun pauseTracking() {
        if (!_isTracking.value || _isPaused.value) return
        _isPaused.value = true
        timerJob?.cancel()
        locationJob?.cancel()
        stepsViewModel.stopTracking()
    }

    fun resumeTracking() {
        if (!_isTracking.value || !_isPaused.value) return
        _isPaused.value = false
        startTimer()
        startLocationUpdates()
        stepsViewModel.resumeTracking()
    }

    fun stopTracking() {
        if (!_isTracking.value) return
        _isTracking.value = false
        _isPaused.value = false
        timerJob?.cancel()
        locationJob?.cancel()
        _speed.value = 0.0
        _pace.value = 0.0
        stepsViewModel.stopTracking()
    }
}

class TrackingViewModelFactory(
    private val ctx: Context,
    private val stepsViewModel: StepsViewModel
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TrackingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TrackingViewModel(ctx, stepsViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
