package dk.zlatan.flotmand.design_system.componenets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.WatchLater
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dk.zlatan.flotmand.R
import dk.zlatan.flotmand.design_system.componenets.buttons.FmAnimatableButton
import dk.zlatan.flotmand.design_system.componenets.spacers.HSpacer
import dk.zlatan.flotmand.design_system.componenets.spacers.VSpacer
import dk.zlatan.flotmand.model.Event
import dk.zlatan.flotmand.model.GeoLocation
import dk.zlatan.flotmand.model.User
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ClosestEventCard(
    modifier: Modifier = Modifier,
    event: Event,
    publisher: User,
    participants: List<User>,
    onParticipateClick: () -> Unit,
    onCardClick: () -> Unit,
    onMapClick: () -> Unit,
    isParticipating: Boolean?,
    isLoading: Boolean,
    isPublisher: Boolean = false,
) {
    val containerColor = MaterialTheme.colorScheme.inverseOnSurface
    val cardShape =
        RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 16.dp,
            bottomStart = 0.dp,
            bottomEnd = 0.dp,
        )

    val geoLocation = event.geoLocation ?: GeoLocation(0.0, 0.0)
    val hasGeo = event.geoLocation?.isValid() == true

    // Format date
    val monthFormatter = DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH)
    val month = event.eventDate?.format(monthFormatter)?.uppercase().orEmpty()
    val day = event.eventDate?.dayOfMonth?.toString().orEmpty()
    // Format time
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val eventTime = event.eventStartTime?.format(timeFormatter).orEmpty()
    val participantsCount = event.participantIds?.size ?: 0

    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onCardClick),
        shape = RoundedCornerShape(6),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        // Hero image / map / placeholder
        var imageError by remember { mutableStateOf(false) }
        val showImage = !event.eventImageUrl.isNullOrBlank() && !imageError

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .height(170.dp)
                    .clip(cardShape.copy(bottomStart = CornerSize(0.dp), bottomEnd = CornerSize(0.dp))),
        ) {
            when {
                showImage -> {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(event.eventImageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize(),
                        onError = { imageError = true },
                    )
                }
                hasGeo -> {
                    AddressMapCard(
                        modifier = Modifier.matchParentSize(),
                        geoLocation = geoLocation,
                        eventDate = event.eventDate,
                        backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                        onClick = onMapClick,
                    )
                }
                else -> {
                    Image(
                        painter = painterResource(R.drawable.flot_image_empty_state),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .matchParentSize()
                            .padding(24.dp),
                    )
                }
            }

            // Full overlay to catch taps
            Box(
                modifier =
                    Modifier
                        .matchParentSize()
                        .clickable(onClick = onMapClick),
            )
        }

        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = event.eventName ?: stringResource(R.string.untitled_event),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )

                    VSpacer(6.dp)

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ProfileImage(
                            profilePic = publisher.photoUrl,
                            userName = publisher.displayName,
                            profileSize = 24.dp,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = publisher.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    VSpacer(8.dp)

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = stringResource(R.string.location_content_description),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = event.location ?: stringResource(R.string.unknown_location),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = month,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = day,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    CapsualWithText(
                        label = eventTime,
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.WatchLater,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    )
                }

            }

            VSpacer(24.dp)

            SeparatorLine(
                modifier = Modifier,
                horizontalPadding = 0.dp,
            )
            VSpacer(16.dp)

            ParticipantsRow(
                participants = participants,
                participantsCount = participantsCount,
                containerColor = containerColor,
                isParticipating = isParticipating,
                isLoading = isLoading,
                onParticipateClick = onParticipateClick,
                isPublisher = isPublisher,
            )
        }
    }
}

@Composable
private fun ParticipantsRow(
    participants: List<User>,
    participantsCount: Int,
    containerColor: Color,
    isParticipating: Boolean?,
    isLoading: Boolean,
    onParticipateClick: () -> Unit,
    isPublisher: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OverlappingAvatars(
            participants = participants,
            containerColor = containerColor,
        )

        // When avatars + button leave too little room, the count text would wrap
        // letter by letter and stretch the row. It's redundant next to the "+X"
        // overflow badge, so hide it instead — the weighted slot stays so the
        // avatars and button keep their positions.
        var countTextOverflows by remember { mutableStateOf(false) }
        Text(
            text = stringResource(R.string.participants_count, participantsCount),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Clip,
            onTextLayout = { countTextOverflows = it.hasVisualOverflow },
            modifier =
                Modifier
                    .weight(1f)
                    .offset(x = (-8).dp)
                    .alpha(if (countTextOverflows) 0f else 1f),
        )

        val participationText =
            if (isParticipating == true) {
                stringResource(R.string.participating)
            } else {
                stringResource(
                    R.string.participate,
                )
            }
        if (!isPublisher) {
            FmAnimatableButton(
                text = participationText,
                onClick = onParticipateClick,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = stringResource(R.string.check_icon_content_description),
                        modifier = Modifier.size(16.dp),
                    )
                },
                isLoading = (isLoading),
                isAffirmed = (isParticipating == true),
                modifier =
                    Modifier
                        .padding(end = 4.dp),
            )
        }
    }
}

@Composable
fun EventCard(
    modifier: Modifier = Modifier,
    userProfilePic: String? = null,
    userName: String,
    eventName: String,
    eventDate: String,
    eventTime: String,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
) {
    val borderColor = MaterialTheme.colorScheme.primary

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (isSelected) Modifier.border(2.dp, borderColor, RoundedCornerShape(12.dp))
                else Modifier,
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.inverseOnSurface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        VSpacer(height = 20.dp)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AnimatedVisibility(
                visible = isSelectionMode,
                enter = expandHorizontally(),
                exit = shrinkHorizontally(),
            ) {
                Row {
                    Icon(
                        imageVector = if (isSelected) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp),
                    )
                    HSpacer(12.dp)
                }
            }

            ProfileImage(
                modifier = Modifier.align(Alignment.CenterVertically),
                profilePic = userProfilePic,
                userName = userName,
                profileSize = 60.dp,
            )

            HSpacer(12.dp)
            Row {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = userName,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        modifier = Modifier,
                    )

                    Text(
                        text = eventName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    VSpacer(4.dp)

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CalendarToday,
                            contentDescription = stringResource(R.string.date_content_description),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp),
                        )
                        HSpacer(8.dp)
                        Text(
                            text = eventDate,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        HSpacer(12.dp)
                        Icon(
                            imageVector = Icons.Filled.WatchLater,
                            contentDescription = stringResource(R.string.date_content_description),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp),
                        )
                        HSpacer(8.dp)
                        Text(
                            text = eventTime,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                AnimatedVisibility(
                    visible = !isSelectionMode,
                    enter = expandHorizontally(),
                    exit = shrinkHorizontally(),
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowForwardIos,
                        contentDescription = stringResource(R.string.location_icon_content_description),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(16.dp)
                            .align(Alignment.CenterVertically),
                    )
                }
            }
        }

        VSpacer(height = 20.dp)
    }
}

@Composable
private fun SeparatorLine(
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 0.dp,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding)
                .height(1.dp)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)),
    )
}

@Preview
@Composable
private fun ClosestEventCardPreview() {
    val mockEvent =
        Event.create(
            eventId = "event1",
            eventName = "The Italian Feast",
            location = "Amager Boul. 101, 2300 København",
            geoLocation = GeoLocation(55.6674, 12.5919),
            eventDate = java.time.LocalDate.of(2026, 1, 28),
            eventStartTime = java.time.LocalTime.of(18, 0),
            publisherId = "publisher1",
            participantIds = listOf("user1", "user2", "user3", "user4", "user5"),
        )

    val mockPublisher =
        User(
            id = "publisher1",
            displayName = "Flotmand",
            email = "flotmand@example.com",
        )

    ClosestEventCard(
        modifier = Modifier,
        event = mockEvent,
        publisher = mockPublisher,
        participants = User.mockUserWithCounter(5),
        onParticipateClick = {},
        onCardClick = {},
        isParticipating = false,
        isLoading = false,
        onMapClick = {},
        isPublisher = false,
    )
}

@Preview(name = "ClosestEventCard — Empty state")
@Composable
private fun ClosestEventCardEmptyStatePreview() {
    val mockEvent =
        Event.create(
            eventId = "event1",
            eventName = "The Italian Feast",
            location = "Amager Boul. 101, 2300 København",
            geoLocation = null,
            eventDate = java.time.LocalDate.of(2026, 1, 28),
            eventStartTime = java.time.LocalTime.of(18, 0),
            publisherId = "publisher1",
            participantIds = listOf("user1", "user2", "user3", "user4", "user5"),
        )

    val mockPublisher =
        User(
            id = "publisher1",
            displayName = "Flotmand",
            email = "flotmand@example.com",
        )

    ClosestEventCard(
        modifier = Modifier,
        event = mockEvent,
        publisher = mockPublisher,
        participants = User.mockUserWithCounter(5),
        onParticipateClick = {},
        onCardClick = {},
        isParticipating = false,
        isLoading = false,
        onMapClick = {},
        isPublisher = false,
    )
}

@Preview()
@Composable
private fun EventCardPreview() {
    EventCard(
        modifier = Modifier,
        userProfilePic = null,
        userName = "Zlatan Stadler",
        eventDate = "06-15",
        eventTime = "18:00",
        eventName = "Middag hos Zlatan",
        onClick = {},
    )
}
