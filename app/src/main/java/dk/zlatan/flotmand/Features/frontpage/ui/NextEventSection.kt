package dk.zlatan.flotmand.Features.frontpage.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dk.zlatan.flotmand.design_system.componenets.ClosestEventCard
import dk.zlatan.flotmand.design_system.componenets.spacers.VSpacer
import dk.zlatan.flotmand.model.Event
import dk.zlatan.flotmand.model.User

@Composable
internal fun NextEventSection(
    modifier: Modifier = Modifier,
    event: Event,
    publisher: User,
    participants: List<User>,
    isParticipating: Boolean,
    isLoading: Boolean,
    onParticipateClick: () -> Unit,
    onCardClick: () -> Unit,
    onMapClick: () -> Unit,
    isPublisher: Boolean,
) {
    Column(modifier = modifier) {

        Text(
            text = "Næste event",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        VSpacer(12.dp)
        ClosestEventCard(
            modifier = Modifier,
            event = event,
            publisher = publisher,
            participants = participants,
            onParticipateClick = onParticipateClick,
            onCardClick = onCardClick,
            onMapClick = onMapClick,
            isParticipating = isParticipating,
            isLoading = isLoading,
            isPublisher = isPublisher,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NextEventSectionPreview() {
    NextEventSection(
        modifier = Modifier,
        event = Event.previewEvents(1).first(),
        publisher = User.mockUserWithCounter(1).first(),
        participants = User.mockUserWithCounter(5),
        onParticipateClick = {},
        onCardClick = {},
        onMapClick = {},
        isParticipating = false,
        isLoading = false,
        isPublisher = false
    )
}