package dk.zlatan.flotmand.design_system.componenets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import dk.zlatan.flotmand.R
import dk.zlatan.flotmand.model.EventStatus
import dk.zlatan.flotmand.model.GeoLocation
import java.time.LocalDate

@Composable
fun AddressMapCard(
    modifier: Modifier = Modifier,
    geoLocation: GeoLocation,
    eventDate: LocalDate?,
    backgroundColor: Color = Color(0xFFE0E0E0),
    onClick: (() -> Unit)? = null,
) {
    val latLng = LatLng(geoLocation.latitude, geoLocation.longitude)

    val cameraPositionState =
        remember(geoLocation) {
            CameraPositionState(
                position =
                    CameraPosition.fromLatLngZoom(
                        LatLng(geoLocation.latitude, geoLocation.longitude),
                        14f,
                    ),
            )
        }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(backgroundColor),
        contentAlignment = Alignment.Center,
    ) {
        // StatusBadge at top left
        if (eventDate != null) {
            Box(
                modifier =
                    Modifier
                        .align(Alignment.TopStart)
                        .zIndex(1f)
                        .padding(8.dp),
            ) {
                StatusCountBadge(
                    eventDate = eventDate,
                )
            }
        }

        val uiSettings =
            MapUiSettings(
                zoomControlsEnabled = false,
                scrollGesturesEnabled = false,
                zoomGesturesEnabled = false,
                rotationGesturesEnabled = false,
                tiltGesturesEnabled = false,
                mapToolbarEnabled = false,
            )

        GoogleMap(
            modifier =
                Modifier
                    .fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = uiSettings,
        ) {
            Marker(
                state = MarkerState(position = latLng),
                title = stringResource(R.string.event_location_marker_title),
            )
        }

        // Full overlay to capture taps (gestures are disabled on the map)
        onClick?.let { clickHandler ->
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .clickable(onClick = clickHandler),
            )
        }
    }
}
