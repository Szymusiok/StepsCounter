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

    private val _pathPoints    = MutableStateFlow<List<LatLng>>(emptyList())
    val pathPoints: StateFlow<List<LatLng>> = _pathPoints.asStateFlow()

    private val _totalDistance = MutableStateFlow(0.0)
    val totalDistance: StateFlow<Double> = _totalDistance.asStateFlow()

    private val _calories      = MutableStateFlow(0.0)
    val calories: StateFlow<Double> = _calories.asStateFlow()

    private val _elapsedTime   = MutableStateFlow(0L)
    val elapsedTime: StateFlow<Long> = _elapsedTime.asStateFlow()

    private var locationJob: Job? = null
    private var timerJob:    Job? = null

    fun startTracking() {
        if (_isTracking.value) return
        _isTracking.value = true

        // reset
        _pathPoints.value    = emptyList()
        _totalDistance.value = 0.0
        _calories.value      = 0.0
        _elapsedTime.value   = 0L

        timerJob = viewModelScope.launch {
            while (_isTracking.value) {
                delay(1_000L)
                _elapsedTime.update { it + 1_000L }
            }
        }

        locationJob = viewModelScope.launch {
            tracker.locationUpdates().collect { newPoint ->
                val prev = _pathPoints.value.lastOrNull()

                _pathPoints.update { old -> old + newPoint }

                prev?.let {
                    val delta = SphericalUtil.computeDistanceBetween(it, newPoint)
                    _totalDistance.update { old -> old + delta }
                }

                _calories.value = (_totalDistance.value / 1_000.0) * 60.0
            }
        }

        stepsViewModel.startTracking()
    }

    fun stopTracking() {
        if (!_isTracking.value) return
        _isTracking.value = false
        timerJob?.cancel()
        locationJob?.cancel()
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