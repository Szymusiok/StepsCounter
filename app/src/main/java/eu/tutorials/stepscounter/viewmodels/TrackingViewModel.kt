package eu.tutorials.stepscounter.viewmodels

import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import eu.tutorials.stepscounter.viewmodels.StepsViewModel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.util.jar.Manifest

class TrackingViewModel(
    private val ctx: Context,
    private val stepsViewModel: StepsViewModel
) : ViewModel() {

    // PUBLIC STATE
    private val _isTracking    = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking

    private val _pathPoints    = MutableStateFlow<List<LatLng>>(emptyList())
    val pathPoints: StateFlow<List<LatLng>> = _pathPoints

    private val _totalDistance = MutableStateFlow(0f)    // in meters
    val totalDistance: StateFlow<Float> = _totalDistance

    private val _calories      = MutableStateFlow(0f)    // in kcal
    val calories: StateFlow<Float> = _calories

    private val _elapsedTime   = MutableStateFlow(0L)    // in ms
    val elapsedTime: StateFlow<Long> = _elapsedTime

    // INTERNAL JOBS
    private var locationJob:    kotlinx.coroutines.Job? = null
    private var timerJob:       kotlinx.coroutines.Job? = null

    /** Starts both step‐count and location updates + timer */
    fun startTracking() {
        if (_isTracking.value) return
        _isTracking.value = true

        // reset session state
        _pathPoints.value    = emptyList()
        _totalDistance.value = 0f
        _calories.value      = 0f
        _elapsedTime.value   = 0L

        // 1) Timer
        timerJob = viewModelScope.launch {
            while (_isTracking.value) {
                delay(1_000L)
                _elapsedTime.value += 1_000L
            }
        }

        // 2) Location updates
        locationJob = viewModelScope.launch {
            getLocationUpdates(ctx)
                .collect { latLng ->
                    // update path
                    val pts = _pathPoints.value.toMutableList()
                    pts.lastOrNull()?.let { prev ->
                        val delta = SphericalUtil.computeDistanceBetween(prev, latLng).toFloat()
                        _totalDistance.value += delta
                    }
                    pts.add(latLng)
                    _pathPoints.value = pts

                    // simple calories: 60 kcal per km
                    _calories.value = (_totalDistance.value / 1_000f) * 60f
                }
        }

        // 3) Steps
        stepsViewModel.startTracking()
    }

    /** Stops everything */
    fun stopTracking() {
        if (!_isTracking.value) return
        _isTracking.value = false
        timerJob?.cancel()
        locationJob?.cancel()
        stepsViewModel.stopTracking()
    }

    /** Emits a flow of LatLng every ~2s */
    private fun getLocationUpdates(ctx: Context): Flow<LatLng> = callbackFlow {
        // 1️⃣ Permission check up front:
        val fineGranted = ContextCompat.checkSelfPermission(
            ctx, android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            ctx, android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!fineGranted && !coarseGranted) {
            // No permissions → close immediately
            close()
            return@callbackFlow
        }

        // 2️⃣ Build your LocationRequest
        val client = LocationServices.getFusedLocationProviderClient(ctx)
        val req = LocationRequest.create().apply {
            interval = 2_000L
            fastestInterval = 1_000L
            priority = Priority.PRIORITY_HIGH_ACCURACY
        }

        // 3️⃣ Callback
        val cb = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.locations.forEach { loc ->
                    trySend(LatLng(loc.latitude, loc.longitude))
                }
            }
        }

        // 4️⃣ Try to start updates
        try {
            client.requestLocationUpdates(req, cb, Looper.getMainLooper())
        } catch (securityEx: SecurityException) {
            // Permissions revoked mid-flight
            close(securityEx)
            return@callbackFlow
        }

        // 5️⃣ Clean up
        awaitClose {
            client.removeLocationUpdates(cb)
        }
    }
}

class TrackingViewModelFactory(
    private val ctx: Context,
    private val stepsViewModel: StepsViewModel
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(TrackingViewModel::class.java) ->
                TrackingViewModel(ctx, stepsViewModel) as T
            else ->
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}