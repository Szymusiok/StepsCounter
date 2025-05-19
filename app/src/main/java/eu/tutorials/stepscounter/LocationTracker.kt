package eu.tutorials.stepscounter

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class LocationTracker(context: Context) {
    private val client = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): LatLng? = suspendCancellableCoroutine { cont ->
        client.lastLocation
            .addOnSuccessListener { loc ->
                if (loc != null) {
                    cont.resume(LatLng(loc.latitude, loc.longitude))
                } else {
                    cont.resume(null)
                }
            }
            .addOnFailureListener { e ->
                cont.resumeWithException(e)
            }
    }
}