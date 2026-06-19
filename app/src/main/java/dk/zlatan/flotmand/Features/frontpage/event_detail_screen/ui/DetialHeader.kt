package dk.zlatan.flotmand.Features.frontpage.event_detail_screen.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.dp
import dk.zlatan.flotmand.R
import dk.zlatan.flotmand.design_system.componenets.StatusCountBadge
import dk.zlatan.flotmand.design_system.componenets.spacers.VSpacer
import java.time.LocalDate

@Composable
internal fun DetailHeader(
    eventDate: LocalDate?,
    eventTitle: String,
    modifier: Modifier = Modifier,
    eventDescription: String? = null,
) {
    Column(
        modifier =
            modifier
                .background(MaterialTheme.colorScheme.surface)
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
    ) {
        if (eventDate != null) {
            StatusCountBadge(
                eventDate = eventDate,
            )
        }

        VSpacer(20.dp)

        // Event title
        Text(
            text = eventTitle,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.fillMaxWidth(),
        )

        VSpacer(12.dp)

        // Event description with Read more/Read less
        eventDescription?.let { description ->
            var isExpanded by remember { mutableStateOf(false) }

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .animateContentSize(
                            animationSpec =
                                spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow,
                                ),
                        ).clickable(
                            indication = null,
                            interactionSource =
                                remember {
                                    androidx.compose.foundation.interaction
                                        .MutableInteractionSource()
                                },
                        ) {
                            isExpanded = !isExpanded
                        },
            ) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = if (isExpanded) Int.MAX_VALUE else 5,
                    overflow = TextOverflow.Ellipsis,
                )

                // Read more/less indicator
                if (description.length > 100) {
                    // Show only if description is long enough
                    Text(
                        text = if (isExpanded) stringResource(R.string.read_less) else stringResource(R.string.read_more),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun DetailHeaderPreview() {
    DetailHeader(
        eventDate = LocalDate.now().plusDays(3),
        eventTitle = "Lasses Italienske Festmiddag",
        eventDescription = LoremIpsum(50).values.first(),
    )
}
