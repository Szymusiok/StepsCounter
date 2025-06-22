package eu.tutorials.stepscounter.databasehelpers

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.android.gms.maps.model.LatLng

// Room entity for saved workouts
@Entity(tableName = "workouts")
@TypeConverters(Converters::class)
data class WorkoutEntity(
    @PrimaryKey val id: String,
    val path: List<LatLng>,
    val distanceMeters: Double,
    val steps: Int,
    val calories: Int,
    val durationMs: Long,
    val timestamp: Long
)