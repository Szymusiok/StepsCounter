package eu.tutorials.stepscounter.model

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp

data class Workout(
    val id: String = "",
    val path: List<LatLng> = emptyList(), // used in-app only
    val distanceMeters: Double = 0.0,
    val steps: Int = 0,
    val calories: Int = 0,
    val durationMs: Long = 0L,
    val timestamp: Timestamp = Timestamp.now()
)

fun List<LatLng>.toFirestorePath(): List<Map<String, Double>> =
    map { mapOf("lat" to it.latitude, "lng" to it.longitude) }

fun List<Map<String, Double>>.toLatLngList(): List<LatLng> =
    mapNotNull { map ->
        val lat = map["lat"]
        val lng = map["lng"]
        if (lat != null && lng != null) LatLng(lat, lng) else null
    }