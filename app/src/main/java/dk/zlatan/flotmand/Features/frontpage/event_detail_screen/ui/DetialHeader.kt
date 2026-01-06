package dk.zlatan.flotmand.Features.frontpage.event_detail_screen.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dk.zlatan.flotmand.design_system.componenets.ProfileImage
import dk.zlatan.flotmand.design_system.componenets.spacers.HSpacer
import dk.zlatan.flotmand.design_system.componenets.spacers.VSpacer
import dk.zlatan.flotmand.model.EventStatus

@Composable
internal fun DetailHeader(
    eventStatus: EventStatus,
    name: String,
    eventTitle: String,
    modifier: Modifier = Modifier,
    publisherProfileImageUrl: String? = null,
    isPublisher: Boolean = false,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    val (statusText, statusColor) = when (eventStatus) {
        EventStatus.UPCOMING -> "Kommende" to Color(0xFF4CAF50) // Green
        EventStatus.ONGOING -> "I gang" to Color(0xFFFFC107)    // Amber
        EventStatus.COMPLETED -> "Afsluttet" to Color(0xFFF44336) // Red
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
    ) {
        // Top-right action icons when publisher: Edit + Delete
        if (isPublisher) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 12.dp, end = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Rediger event",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Slet event",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            VSpacer(120.dp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProfileImage(
                    profilePic = publisherProfileImageUrl,
                    userName = name,
                    profileSize = 64.dp,
                )
                HSpacer(12.dp)
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.height(IntrinsicSize.Min),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .alignByBaseline()
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                        Text(
                            text = "Status: $statusText",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier
                                .alignByBaseline()
                                .padding(start = 8.dp)
                        )
                    }
                    VSpacer(12.dp)
                    Text(
                        text = name,
                        style = MaterialTheme.typography.displaySmall
                    )
                    VSpacer(12.dp)
                    Text(
                        text = eventTitle,
                        style = MaterialTheme.typography.labelMedium
                    )
                    VSpacer(18.dp)
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun DetailHeaderPreview() {
    DetailHeader(
        eventStatus = EventStatus.UPCOMING,
        name = "Mikkel",
        eventTitle = "Lækker sejlads på Øresund",
        isPublisher = true,
        onEditClick = {},
        onDeleteClick = {}
    )
}