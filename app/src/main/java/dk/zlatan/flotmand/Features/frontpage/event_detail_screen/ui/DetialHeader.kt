package dk.zlatan.flotmand.Features.frontpage.event_detail_screen.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dk.zlatan.flotmand.Features.frontpage.model.EventStatus
import dk.zlatan.flotmand.design_system.componenets.spacers.VSpacer

@Composable
internal fun DetailHeader(
    modifier: Modifier = Modifier,
    eventStatus: EventStatus,
    name: String,
) {
    val (statusText, statusColor) = when (eventStatus) {
        EventStatus.UPCOMING -> "Kommende" to Color(0xFF4CAF50) // Green
        EventStatus.ONGOING -> "I gang" to Color(0xFFFFC107)    // Amber
        EventStatus.COMPLETED -> "Afsluttet" to Color(0xFFF44336) // Red
    }
    Column(modifier = modifier
        .fillMaxWidth()
        .background(MaterialTheme.colorScheme.primaryContainer)
        .padding(horizontal = 20.dp)
    ) {
        VSpacer(120.dp)
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
                modifier = Modifier.alignByBaseline().padding(start = 8.dp)
            )
        }
        VSpacer(12.dp)
        Text(
            text = name,
            style = MaterialTheme.typography.displaySmall
        )
        VSpacer(18.dp)
    }
}

@PreviewLightDark
@Composable
private fun DetailHeaderPreview() {
    DetailHeader(
        modifier = Modifier,
        eventStatus = EventStatus.UPCOMING,
        name = "Mikkel"
    )
}