package eu.tutorials.stepscounter.databasehelpers

import androidx.room.TypeConverter
import com.google.android.gms.maps.model.LatLng

// Converts complex types for Room
class Converters {
    @TypeConverter
    fun pathToString(path: List<LatLng>): String =
        path.joinToString(";") { "${it.latitude},${it.longitude}" }

    @TypeConverter
    fun stringToPath(data: String): List<LatLng> =
        if (data.isBlank()) emptyList() else data.split(";").mapNotNull {
            val parts = it.split(",")
            val lat = parts.getOrNull(0)?.toDoubleOrNull()
            val lng = parts.getOrNull(1)?.toDoubleOrNull()
            if (lat != null && lng != null) LatLng(lat, lng) else null
        }
}