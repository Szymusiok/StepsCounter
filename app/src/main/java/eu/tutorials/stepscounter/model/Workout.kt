package eu.tutorials.stepscounter.model

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import eu.tutorials.stepscounter.databasehelpers.WorkoutEntity
import java.util.Date
import java.util.UUID

// Represents a single workout session
data class Workout(
    val id: String = "",
    val path: List<LatLng> = emptyList(),
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

fun Workout.toEntity(): WorkoutEntity = WorkoutEntity(
    id = if (id.isNotEmpty()) id else UUID.randomUUID().toString(),
    path = path,
    distanceMeters = distanceMeters,
    steps = steps,
    calories = calories,
    durationMs = durationMs,
    timestamp = timestamp.toDate().time
)

fun WorkoutEntity.toModel(): Workout = Workout(
    id = id,
    path = path,
    distanceMeters = distanceMeters,
    steps = steps,
    calories = calories,
    durationMs = durationMs,
    timestamp = Timestamp(Date(timestamp))
)