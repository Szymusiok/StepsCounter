package eu.tutorials.stepscounter.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import eu.tutorials.stepscounter.LocationTracker
import eu.tutorials.stepscounter.TrackingService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// Exposes data from TrackingService to the UI
class TrackingViewModel(
    private val ctx: Context
) : ViewModel() {

    val isTracking: StateFlow<Boolean> = TrackingService.isTracking
    val isPaused: StateFlow<Boolean> = TrackingService.isPaused
    val pathPoints: StateFlow<List<LatLng>> = TrackingService.pathPoints
    val totalDistance: StateFlow<Double> = TrackingService.totalDistance
    val calories: StateFlow<Double> = TrackingService.calories
    val elapsedTime: StateFlow<Long> = TrackingService.elapsedTime
    val speed: StateFlow<Double> = TrackingService.speed
    val pace: StateFlow<Double> = TrackingService.pace
    val steps: StateFlow<Int> = TrackingService.steps

    fun startTracking() = TrackingService.start(ctx)
    fun pauseTracking() = TrackingService.pause(ctx)
    fun resumeTracking() = TrackingService.resume(ctx)
    fun stopTracking() = TrackingService.stop(ctx)

}

class TrackingViewModelFactory(
    private val ctx: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TrackingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TrackingViewModel(ctx) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
