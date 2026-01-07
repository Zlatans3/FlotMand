package dk.zlatan.flotmand.Features.frontpage.event_detail_screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dk.zlatan.flotmand.Features.frontpage.event_detail_screen.ui.AddressMapCard
import dk.zlatan.flotmand.Features.frontpage.event_detail_screen.ui.DetailHeader
import dk.zlatan.flotmand.Features.frontpage.event_detail_screen.ui.ParticipantsBottomSheet
import dk.zlatan.flotmand.Features.frontpage.event_detail_screen.ui.SectionItem
import dk.zlatan.flotmand.design_system.componenets.dialogs.FmConfirmDialog
import dk.zlatan.flotmand.design_system.componenets.spacers.VSpacer
import dk.zlatan.flotmand.design_system.icon.FmIcons
import dk.zlatan.flotmand.model.Event
import dk.zlatan.flotmand.model.EventStatus
import dk.zlatan.flotmand.model.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EventDetailScreenRoute(
    eventId: String,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    viewModel: EventDetailViewModel = hiltViewModel<EventDetailViewModel, EventDetailViewModel.Factory>(
        key = eventId,
        creationCallback = { factory ->
            factory.create(eventId)
        }
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isDeleted by viewModel.isDeleted.collectAsState(initial = false)
    val clipboardManager = LocalClipboardManager.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (isDeleted) {
        onDismiss()
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        when {
            uiState.isLoadingEvent -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.padding(8.dp))
                    Text(text = "Henter et flot event...")
                }
            }

            uiState.event == null -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Event ikke fundet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.padding(8.dp))
                    Text(
                        text = "Eventet kunne ikke indlæses eller eksisterer ikke",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            else -> {
                EventDetailScreenContent(
                    event = uiState.event!!,
                    isParticipating = uiState.isParticipated,
                    publisher = uiState.publisher,
                    onParticipantsClick = {
                        if (!uiState.event!!.participantIds.isNullOrEmpty()) {
                            viewModel.showParticipants()
                        }
                    },
                    onDateClick = {
                        val dateTimeString = "${
                            uiState.event!!.eventDate?.toString().orEmpty()
                        } ${uiState.event!!.eventStartTime.toString()}"
                        clipboardManager.setText(AnnotatedString(dateTimeString))
                    },
                    onLocationLongClick = {
                        val locationString = uiState.event!!.location.orEmpty()
                        clipboardManager.setText(AnnotatedString(locationString))
                    },
                    onEditEvent = {
                        // TODO: Zlatan 06/01/2026 WILL BE ADDED IN LATER PATCH
                    },
                    onDeleteEvent = { showDeleteDialog = true },
                    isPublisher = uiState.isPublisher,
                    onParticipateClick = {
                        viewModel.onUserParticipate()
                    }
                )
            }
        }
    }

    if (uiState.showParticipationBottomSheet) {
        ParticipantsBottomSheet(
            onDismiss = {
                viewModel.onDismissParticipantsSheet()
            },
            participants = uiState.participants,
            publisherId = uiState.publisher?.id
        )
    }

    if (showDeleteDialog) {
        FmConfirmDialog(
            title = "Slet event?",
            message = "Denne handling kan ikke fortrydes.",
            confirmText = "Slet",
            onDismiss = { showDeleteDialog = false },
            onConfirmClick = {
                showDeleteDialog = false
                viewModel.deleteEvent(eventId)
            }
        )
    }
}

@Composable
private fun EventDetailScreenContent(
    modifier: Modifier = Modifier,
    event: Event,
    isParticipating: Boolean?,
    publisher: User?,
    isPublisher: Boolean,
    onParticipantsClick: () -> Unit,
    onParticipateClick: () -> Unit,
    onEditEvent: () -> Unit,
    onDeleteEvent: () -> Unit,
    onDateClick: () -> Unit,
    onLocationLongClick: () -> Unit,
) {
    val eventOrganizerName = if (isPublisher) "Dig" else publisher?.displayName.orEmpty()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) // Use background color
    ) {
        DetailHeader(
            eventStatus = event.status,
            name = eventOrganizerName,
            eventTitle = event.eventName.orEmpty(),
            publisherProfileImageUrl = publisher?.photoUrl,
            isPublisher = isPublisher,
            onEditClick = onEditEvent,
            onDeleteClick = onDeleteEvent
        )
        VSpacer(20.dp)
        SectionItem(
            modifier = Modifier,
            leadingIcon = FmIcons.Calendar,
            title = "${event.eventDate?.toString().orEmpty()} ${event.eventStartTime.toString()}",
            onLongClick = onDateClick,
            iconTint = MaterialTheme.colorScheme.primary,
            textColor = MaterialTheme.colorScheme.onSurface
        )

        SectionItem(
            modifier = Modifier,
            leadingIcon = FmIcons.Person,
            trailingIcon = FmIcons.chevronRight,
            title = "Deltagere: ${event.participantIds?.size ?: 0}/6",
            onClick = onParticipantsClick,
            iconTint = MaterialTheme.colorScheme.secondary,
            textColor = MaterialTheme.colorScheme.onSurface
        )

        SectionItem(
            modifier = Modifier,
            leadingIcon = FmIcons.mapPin,
            title = event.location.orEmpty(),
            onLongClick = onLocationLongClick,
            iconTint = MaterialTheme.colorScheme.tertiary,
            textColor = MaterialTheme.colorScheme.onSurface
        )

        VSpacer(20.dp)

        AddressMapCard(
            modifier = Modifier
                .padding(horizontal = 20.dp),
            backgroundColor = MaterialTheme.colorScheme.surfaceVariant
        )

        Spacer(modifier = Modifier.weight(1f))
        val participationText = if (isParticipating == true) {
            "deltager"
        } else {
            "Deltag"
        }
        if (!isPublisher && event.status == EventStatus.UPCOMING) {
            Button(
                onClick = onParticipateClick,
                shape = RoundedCornerShape(8.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 2.dp
                ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.animateContentSize()
                ) {
                    AnimatedContent(
                        targetState = isParticipating,
                        transitionSpec = {
                            (fadeIn(tween(200)) togetherWith fadeOut(tween(200))).using(SizeTransform(false))
                        }
                    ) { participatingState ->
                        when (participatingState) {
                            true -> {
                                val scale = remember { Animatable(0f) }

                                LaunchedEffect(participatingState) {
                                    scale.animateTo(
                                        targetValue = 1f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioNoBouncy,
                                            stiffness = Spring.StiffnessLow,
                                            visibilityThreshold = 1f
                                        )
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = FmIcons.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier
                                            .size(20.dp)
                                            .graphicsLayer {
                                                scaleX = scale.value
                                                scaleY = scale.value
                                            }
                                    )
                                    Spacer(modifier = Modifier.size(8.dp))
                                }
                            }
                            null -> {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.size(8.dp))
                                }
                            }
                            false -> {
                                // Render empty space for alignment consistency
                                Spacer(modifier = Modifier.size(0.dp))
                            }
                        }
                    }
                    Text(text = participationText)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EventDetailScreenPreview() {
    val event = Event.staticTestEvents[0]
    EventDetailScreenContent(
        modifier = Modifier,
        event = event,
        publisher = User(
            id = "test-publisher",
            displayName = "Lasse Sandø",
            email = "publisher@test.com"
        ),
        onLocationLongClick = {},
        onDateClick = {},
        onEditEvent = {},
        onDeleteEvent = {},
        isPublisher = false,
        onParticipantsClick = {},
        onParticipateClick = {},
        isParticipating = true
    )
}

@Preview(showBackground = true)
@Composable
private fun EventDetailScreenIsPublisherPreview() {
    val event = Event.staticTestEvents.first()
    EventDetailScreenContent(
        modifier = Modifier,
        event = event,
        publisher = User(
            id = "user1",
            displayName = "Zlatan Stadler",
            email = "publisher@test.com",
        ),
        onLocationLongClick = {},
        onDateClick = {},
        onEditEvent = {},
        onDeleteEvent = {},
        isPublisher = true,
        onParticipantsClick = {},
        onParticipateClick = {},
        isParticipating = false
    )
}

@Preview(showBackground = true)
@Composable
private fun PrintListPreview() {
    val event = Event.staticTestEvents.first()
    val publisher = User(
        id = "test-publisher",
        displayName = "Test Publisher",
        email = "publisher@test.com"
    )
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Event: ${event.eventName}\nDate: ${event.eventDate}\nTime: ${event.eventStartTime}\nHost: ${publisher.displayName}"
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EventDetailScreenLoadingPreview() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.padding(8.dp))
        Text(text = "Henter et flot event...")
    }
}
