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

class LocationTracker(private val context: Context) {

    private val client = LocationServices.getFusedLocationProviderClient(context)

    /** One-shot “last known” location, or null if none available. */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): LatLng? = suspendCancellableCoroutine { cont ->
        client.lastLocation
            .addOnSuccessListener { loc ->
                if (loc != null) cont.resume(LatLng(loc.latitude, loc.longitude))
                else           cont.resume(null)
            }
            .addOnFailureListener { e ->
                cont.resumeWithException(e)
            }
    }

    /** Continuous updates every ~intervalMs. */
    @SuppressLint("MissingPermission")
    fun locationUpdates(
        intervalMs: Long = 2_000L,
        fastestMs: Long = 1_000L
    ): Flow<LatLng> = callbackFlow {
        val fine = ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!fine && !coarse) {
            close(SecurityException("Location permission not granted"))
            return@callbackFlow
        }

        val req = LocationRequest.create().apply {
            interval = intervalMs
            fastestInterval = fastestMs
            priority = Priority.PRIORITY_HIGH_ACCURACY
        }
        val cb = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let {
                    trySend(LatLng(it.latitude, it.longitude))
                }
            }
        }

        client.requestLocationUpdates(req, cb, Looper.getMainLooper())
        awaitClose { client.removeLocationUpdates(cb) }
    }
}