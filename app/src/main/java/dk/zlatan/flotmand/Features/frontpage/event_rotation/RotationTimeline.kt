@file:Suppress(
    "ktlint:standard:function-naming",
    "ktlint:standard:max-line-length",
    "ktlint:standard:multiline-expression-wrapping",
    "ktlint:standard:multiline-if-else",
    "ktlint:standard:blank-line-between-when-conditions",
    "ktlint:standard:package-name",
)

package dk.zlatan.flotmand.Features.frontpage.event_rotation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dk.zlatan.flotmand.design_system.componenets.ProfileImage
import dk.zlatan.flotmand.design_system.componenets.spacers.VSpacer
import dk.zlatan.flotmand.design_system.theme.FlotMandTheme

private val CardWidth = 88.dp
private val AvatarSize = 56.dp

@Composable
fun RotationTimeline(
    items: List<RotationTimelineItem>,
    onNormalCardClick: (monthId: String, hostId: String, hostName: String) -> Unit,
    onVacantCardClick: (monthId: String) -> Unit,
    showAddSelf: Boolean = false,
    onAddSelf: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(items, key = { it.monthId }) { item ->
            when (item) {
                is RotationTimelineItem.Normal -> NormalRotationCard(
                    item = item,
                    onClick = { onNormalCardClick(item.monthId, item.hostId, item.hostName) },
                )

                is RotationTimelineItem.Vacant -> VacantRotationCard(
                    item = item,
                    onClick = { onVacantCardClick(item.monthId) },
                )
            }
        }
        if (showAddSelf) {
            item(key = "add_self") {
                AddSelfCard(onClick = onAddSelf)
            }
        }
    }
}

@Composable
private fun NormalRotationCard(
    item: RotationTimelineItem.Normal,
    onClick: () -> Unit,
) {
    val containerColor = if (item.isCurrent) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }
    val contentColor = if (item.isCurrent) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val labelColor = if (item.isCurrent) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(CardWidth),
    ) {
        MonthLabel(
            label = item.monthLabel,
            color = labelColor,
            isCurrent = item.isCurrent,
        )

        VSpacer(6.dp)

        Card(
            onClick = onClick,
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = if (item.isCurrent) 6.dp else 2.dp),
            colors = CardDefaults.cardColors(containerColor = containerColor),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 16.dp),
            ) {
                ProfileImage(
                    profilePic = item.hostPhotoUrl,
                    profileSize = AvatarSize,
                    userName = item.hostName,
                )

                VSpacer(10.dp)

                Text(
                    text = item.hostName.split(" ").first(),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun VacantRotationCard(
    item: RotationTimelineItem.Vacant,
    onClick: () -> Unit,
) {
    val borderColor = MaterialTheme.colorScheme.outline
    val contentColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(CardWidth),
    ) {
        MonthLabel(
            label = item.monthLabel,
            color = contentColor,
            isCurrent = item.isCurrent,
        )

        VSpacer(6.dp)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable(onClick = onClick)
                .dashedBorder(color = borderColor, cornerRadius = 16.dp)
                .padding(horizontal = 10.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(
                    modifier = Modifier
                        .size(AvatarSize)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(22.dp),
                    )
                }

                VSpacer(10.dp)

                Text(
                    text = "Ledig plads",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = contentColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun MonthLabel(
    label: String,
    color: Color,
    isCurrent: Boolean,
) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
        color = color,
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun AddSelfCard(onClick: () -> Unit) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val containerColor = MaterialTheme.colorScheme.primaryContainer
    val onContainerColor = MaterialTheme.colorScheme.onPrimaryContainer

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(CardWidth),
    ) {
        MonthLabel(
            label = "Tilmeld",
            color = primaryColor,
            isCurrent = false,
        )

        VSpacer(6.dp)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable(onClick = onClick)
                .dashedBorder(color = primaryColor, cornerRadius = 16.dp)
                .padding(horizontal = 10.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(
                    modifier = Modifier
                        .size(AvatarSize)
                        .clip(CircleShape)
                        .background(containerColor),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.PersonAdd,
                        contentDescription = null,
                        tint = onContainerColor,
                        modifier = Modifier.size(22.dp),
                    )
                }

                VSpacer(10.dp)

                Text(
                    text = "Dig",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = primaryColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

private fun Modifier.dashedBorder(
    color: Color,
    cornerRadius: Dp,
    strokeWidth: Dp = 1.5.dp,
): Modifier =
    this.drawBehind {
        val cornerRadiusPx = cornerRadius.toPx()
        val stroke = Stroke(
            width = strokeWidth.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f)),
        )
        drawRoundRect(
            color = color,
            cornerRadius = CornerRadius(cornerRadiusPx),
            style = stroke,
        )
    }

// region Previews

private val previewItems = listOf(
    RotationTimelineItem.Normal("2026-06", "Juni", isCurrent = true, "uid_david", "David Sandell", null),
    RotationTimelineItem.Normal("2026-07", "Juli", isCurrent = false, "uid_lasse", "Lasse Sandø", null),
    RotationTimelineItem.Vacant("2026-08", "August", isCurrent = false),
    RotationTimelineItem.Normal("2026-09", "September", isCurrent = false, "uid_oliver", "Oliver Payne", null),
    RotationTimelineItem.Normal("2026-10", "Oktober", isCurrent = false, "uid_mikkel", "Mikkel Rahbek", null),
)

@PreviewLightDark
@Composable
private fun RotationTimelinePreview() {
    FlotMandTheme {
        RotationTimeline(
            items = previewItems,
            onNormalCardClick = { _, _, _ -> },
            onVacantCardClick = {},
            showAddSelf = true,
            onAddSelf = {},
        )
    }
}

// endregion
