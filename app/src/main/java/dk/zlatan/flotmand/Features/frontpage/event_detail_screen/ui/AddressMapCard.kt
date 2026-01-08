package dk.zlatan.flotmand.Features.frontpage.event_detail_screen.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import dk.zlatan.flotmand.model.GeoLocation

@Composable
internal fun AddressMapCard(
    modifier: Modifier = Modifier,
    geoLocation: GeoLocation,
    backgroundColor: Color = Color(0xFFE0E0E0)
) {
    val latLng = LatLng(geoLocation.latitude, geoLocation.longitude)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(latLng, 14f)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
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
                modifier = Modifier.fillMaxWidth().height(160.dp),
                cameraPositionState = cameraPositionState,
                uiSettings = uiSettings
            ) {
                Marker(
                    state = MarkerState(position = latLng),
                    title = "Event location"
                )
            }
        }
    }
}

@Preview
@Composable
private fun AddressMapCardPreview() {
    AddressMapCard(
        modifier = Modifier,
        geoLocation = GeoLocation(latitude = 55.6761, longitude = 12.5683) // Copenhagen
    )
}