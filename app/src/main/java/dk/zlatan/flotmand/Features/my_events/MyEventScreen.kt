package dk.zlatan.flotmand.Features.my_events

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dk.zlatan.flotmand.Features.my_events.ui.MyEventContent
import dk.zlatan.flotmand.Features.my_events.ui.MyEventEmptyState
import dk.zlatan.flotmand.Features.my_events.ui.MyEventErrorState
import dk.zlatan.flotmand.Features.my_events.ui.MyEventLoadingState
import dk.zlatan.flotmand.Features.my_events.ui.MyEventTopBar
import dk.zlatan.flotmand.R
import dk.zlatan.flotmand.design_system.icon.FmIcons
import dk.zlatan.flotmand.model.Event
import dk.zlatan.flotmand.model.User

@Composable
internal fun MyEventScreenRoute(
    modifier: Modifier = Modifier,
    viewModel: MyEventViewModel = hiltViewModel(),
    onAddEventClick: () -> Unit = {},
    onEventClick: (String) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { MyEventTopBar() },
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                MyEventLoadingState(modifier = modifier)
            }

            uiState.errorMessage != null -> {
                MyEventErrorState(
                    errorMessage = uiState.errorMessage!!,
                    modifier = modifier,
                )
            }

            uiState.eventList.isEmpty() -> {
                MyEventEmptyState(
                    onAddEventClick = onAddEventClick,
                    modifier = modifier.padding(paddingValues),
                )
            }

            else -> {
                MyEventScreen(
                    modifier = modifier.padding(top = paddingValues.calculateTopPadding()),
                    eventList = uiState.eventList,
                    publishers = uiState.publishers,
                    onAddEventClick = onAddEventClick,
                    onEventClick = onEventClick,
                )
            }
        }
    }
}

@Composable
internal fun MyEventScreen(
    eventList: List<Event>,
    publishers: Map<String, User>,
    modifier: Modifier = Modifier,
    onAddEventClick: () -> Unit = {},
    onEventClick: (String) -> Unit = {},
) {
    Box(modifier = modifier.fillMaxSize()) {
        MyEventContent(
            modifier = Modifier.matchParentSize(),
            eventList = eventList,
            publishers = publishers,
            onEventClick = onEventClick,
        )
        ExtendedFloatingActionButton(
            onClick = onAddEventClick,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier =
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp),
        ) {
            Icon(FmIcons.Add, contentDescription = stringResource(R.string.add_event_content_description))
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.create_event), style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MyEventScreenPreview() {
    val mockPublishers =
        mapOf(
            "publisher1" to
                User(
                    id = "publisher1",
                    displayName = "John Doe",
                    email = "john@example.com",
                ),
        )
    MyEventContent(
        eventList = Event.previewEvents(2),
        publishers = mockPublishers,
    )
}
