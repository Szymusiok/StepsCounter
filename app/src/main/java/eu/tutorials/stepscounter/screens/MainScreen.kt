package eu.tutorials.stepscounter.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import eu.tutorials.stepscounter.LocationTracker

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainScreen(
) {
    val context = LocalContext.current
    val permissions = rememberMultiplePermissionsState(
        permissions = listOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )
    var currentLoc by remember { mutableStateOf<LatLng?>(null) }
    val cameraState = rememberCameraPositionState()

    // 1) Request permissions
    LaunchedEffect(Unit) {
        permissions.launchMultiplePermissionRequest()
    }

    // 2) Fetch location when granted
    LaunchedEffect(permissions.allPermissionsGranted) {
        if (permissions.allPermissionsGranted) {
            val loc = LocationTracker(context).getCurrentLocation()
            loc?.let {
                currentLoc = it
                cameraState.position = CameraPosition.fromLatLngZoom(it, 15f)
            }
        }
    }

    // 3) Show map or prompt
    if (permissions.allPermissionsGranted && currentLoc != null) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraState,
            properties = MapProperties(isMyLocationEnabled = true),
            uiSettings = MapUiSettings(myLocationButtonEnabled = true)
        ) {
            Marker(
                state = MarkerState(position = currentLoc!!),
                title = "You are here"
            )
        }
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Grant location permission to view map")
        }
    }
}