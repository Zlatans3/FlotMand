package dk.zlatan.flotmand.Features.my_events

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dk.zlatan.flotmand.model.Event

@Composable
internal fun MyEventScreen(
 modifier: Modifier = Modifier,
 viewModel : MyEventViewModel = hiltViewModel()
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val events = viewModel.myDinnerEvents.collectAsStateWithLifecycle()
        val currentUserId = viewModel.currentUserId
        val filteredEvents = events.value.filter { it.publisherId == currentUserId }
        MyEventContent(
            modifier = modifier,
            filteredEvents = filteredEvents,
        )
    }
}

@Composable
fun MyEventContent(
    modifier: Modifier = Modifier,
    filteredEvents: List<Event>,

    ) {
    Column(modifier = Modifier) {
        if (filteredEvents.isEmpty()) {
            Text(text = "No events found.")
        } else {
            filteredEvents.forEach { event ->
                Text(text = event.eventId.orEmpty())
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MyEventScreenPreview() {
    MyEventScreen(
        modifier = Modifier,
    )
}