package dk.zlatan.flotmand.Features.frontpage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dk.zlatan.flotmand.Features.frontpage.model.Event
import dk.zlatan.flotmand.Features.frontpage.model.Event.Companion.previewEvents
import dk.zlatan.flotmand.Features.frontpage.ui.EventCard
import dk.zlatan.flotmand.Features.frontpage.ui.FrontPageHeader
import dk.zlatan.flotmand.design_system.componenets.spacers.VSpacer
import dk.zlatan.flotmand.design_system.theme.FlotMandTheme
import dk.zlatan.flotmand.model.User

@Composable
fun FrontPageRoute(
    modifier: Modifier = Modifier,
    onClickEvent: (String) -> Unit,
    viewModel: FrontPageViewModel = hiltViewModel(),
    ) {

    FrontpageContent(
        modifier = modifier,
        onClickEvent = onClickEvent,
        eventList = viewModel.eventList,
        user = null
    )
}

@Composable
fun FrontpageContent(
    modifier: Modifier = Modifier,
    eventList: List<Event> = emptyList(),
    onClickEvent: (String) -> Unit,
    user: User?,
) {
    LazyColumn(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceContainer),
    ) {
        item {
            FrontPageHeader()
            VSpacer(12.dp)
            if (user != null) {
                Text(
                    "Bruger: ${user.displayName} (${user.email})",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                VSpacer(8.dp)
            }
        }
        items(eventList) { eventDetails ->
            EventCard(
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp),
                userName = eventDetails.publisher?.displayName.orEmpty(),
                eventName = eventDetails.eventName.orEmpty(),
                eventDate = eventDetails.eventDate.toString(),
                eventTime = eventDetails.eventStartTime.toString(),
                onClick = {
                    onClickEvent(eventDetails.eventId.orEmpty())
                }
            )
        }

    }
}

@Preview(showBackground = true, showSystemUi = true)
@PreviewLightDark()
@Composable
private fun FrontpageContentPreview() {
    val events = previewEvents(5)
    FlotMandTheme {
        FrontpageContent(
            modifier = Modifier,
            eventList = events,
            onClickEvent = {},
            user = null
        )
    }
}