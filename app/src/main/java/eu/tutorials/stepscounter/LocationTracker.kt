package eu.tutorials.stepscounter

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

// Small helper over FusedLocationProviderClient.
// Used to get one-time or continuous location data.
class LocationTracker(private val context: Context) {

    private val locationClient = LocationServices.getFusedLocationProviderClient(context)

    // Return the last known location or null
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): LatLng? = suspendCancellableCoroutine { cont ->
        locationClient.lastLocation
            .addOnSuccessListener { location ->
                cont.resume(location?.let { LatLng(it.latitude, it.longitude) })
            }
            .addOnFailureListener { exception ->
                cont.resumeWithException(exception)
            }
    }

    // Emit location changes as a Flow
    @SuppressLint("MissingPermission")
    fun locationUpdates(
        intervalMs: Long = 2_000L,
        fastestMs: Long = 1_000L
    ): Flow<LatLng> = callbackFlow {
        if (!hasLocationPermission()) {
            close(SecurityException("Location permission not granted"))
            return@callbackFlow
        }

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMs)
            .setMinUpdateIntervalMillis(fastestMs)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let {
                    trySend(LatLng(it.latitude, it.longitude)).isSuccess
                }
            }
        }

        locationClient.requestLocationUpdates(request, callback, Looper.getMainLooper())
        awaitClose { locationClient.removeLocationUpdates(callback) }
    }

    // Check if either fine or coarse location permission is granted
    private fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }
}
