package dk.zlatan.flotmand.design_system.componenets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import dk.zlatan.flotmand.model.GeoLocation

@Composable
fun AddressMapCard(
    modifier: Modifier = Modifier,
    geoLocation: GeoLocation,
    backgroundColor: Color = Color(0xFFE0E0E0),
    onClick: (() -> Unit)? = null
) {
    val latLng = LatLng(geoLocation.latitude, geoLocation.longitude)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(latLng, 14f)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        val uiSettings = MapUiSettings(
            zoomControlsEnabled = false,
            scrollGesturesEnabled = false,
            zoomGesturesEnabled = false,
            rotationGesturesEnabled = false,
            tiltGesturesEnabled = false,
            mapToolbarEnabled = false
        )

        GoogleMap(
            modifier = Modifier
                .fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = uiSettings
        ) {
            Marker(
                state = MarkerState(position = latLng),
                title = "Event location"
            )
        }

        // Full overlay to capture taps (gestures are disabled on the map)
        onClick?.let { clickHandler ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(onClick = clickHandler)
            )
        }
    }
}
