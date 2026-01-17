package dk.zlatan.flotmand.design_system.componenets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import dk.zlatan.flotmand.model.EventStatus

@Composable
fun StatusBadge(
    eventStatus: EventStatus,
    modifier: Modifier = Modifier,
) {
    val (statusText, statusColor) = when (eventStatus) {
        EventStatus.UPCOMING -> "Kommende" to MaterialTheme.colorScheme.primary
        EventStatus.ONGOING -> "I gang" to MaterialTheme.colorScheme.tertiary
        EventStatus.COMPLETED -> "Færdig" to MaterialTheme.colorScheme.outline
    }


    // Status badge
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .padding(end = 6.dp)
                    .clip(CircleShape)
                    .background(statusColor)
                    .padding(4.dp)
            )
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun StatusCountBadge(
    eventDate: LocalDate,
    modifier: Modifier = Modifier,
) {
    val daysTillEvent = eventDate.toEpochDay() - LocalDate.now().toEpochDay()
    val (statusBarLabel, statusColor) = when {
        daysTillEvent > 1 -> "$daysTillEvent dage indtil event" to MaterialTheme.colorScheme.primary
        daysTillEvent == 1L -> "I morgen" to MaterialTheme.colorScheme.tertiary
        daysTillEvent == 0L -> "Event i dag" to MaterialTheme.colorScheme.secondary
        else -> "Event er afsluttet" to MaterialTheme.colorScheme.outline
    }
    // Status badge
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .padding(end = 6.dp)
                    .clip(CircleShape)
                    .background(statusColor)
                    .padding(4.dp)
            )
            Text(
                text = statusBarLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview
@Composable
private fun StatusBadgePreview() {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatusBadge(
            eventStatus = EventStatus.UPCOMING
        )
        StatusBadge(
            eventStatus = EventStatus.ONGOING
        )
        StatusBadge(
            eventStatus = EventStatus.COMPLETED
        )
    }
}

@Preview
@Composable
private fun StatusCountBadgePreview() {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatusCountBadge(
            eventDate = LocalDate.now().plusDays(5)
        )
        StatusCountBadge(
            eventDate = LocalDate.now().plusDays(1)
        )
        StatusCountBadge(
            eventDate = LocalDate.now()
        )
        StatusCountBadge(
            eventDate = LocalDate.now().minusDays(2)
        )
    }
}