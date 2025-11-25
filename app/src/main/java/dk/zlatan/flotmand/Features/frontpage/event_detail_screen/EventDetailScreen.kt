package dk.zlatan.flotmand.Features.frontpage.event_detail_screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
internal fun EventDetailScreenRoute(
    modifier : Modifier = Modifier,
    eventId: String?,
    viewModel: EventDetailViewModel = hiltViewModel()
) {
    EventDetailScreenContent(
    modifier = modifier,
    eventId = eventId
    )
}

@Composable
private fun EventDetailScreenContent(
    modifier: Modifier = Modifier,
    eventId: String?
    ) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if(eventId != null) {
            Text(
                text = "Event Detail Screen for eventId:\n$eventId"
            )
        } else {
            Text(
                text = "Event Detail Screen: No eventId provided"
            )
        }
    }
}

@Preview
@Composable
private fun EventDetailScreenPreview() {
    EventDetailScreenContent(
        modifier = Modifier,
        eventId = "preview_event_id"
    )
}