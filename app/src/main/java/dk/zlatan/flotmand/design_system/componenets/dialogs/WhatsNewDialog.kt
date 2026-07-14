@file:Suppress("ktlint:standard:function-naming", "ktlint:standard:package-name")

package dk.zlatan.flotmand.design_system.componenets.dialogs

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dk.zlatan.flotmand.R
import dk.zlatan.flotmand.design_system.componenets.CapsualWithText
import dk.zlatan.flotmand.design_system.componenets.buttons.FmPrimaryButton
import dk.zlatan.flotmand.design_system.componenets.spacers.VSpacer
import kotlinx.coroutines.launch
import kotlin.math.abs

enum class ChangeType { NEW, IMPROVED, FIXED }

data class WhatsNewChange(
    val type: ChangeType,
    val title: String,
    val description: String? = null,
)

@Composable
fun FmWhatsNewDialog(
    modifier: Modifier = Modifier,
    versionName: String,
    changes: List<WhatsNewChange>,
    onDismiss: () -> Unit,
) {
    val pagerState = rememberPagerState { changes.size }
    val coroutineScope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage >= changes.lastIndex

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier =
                        Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(12.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Celebration,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(28.dp),
                    )
                }
                VSpacer(12.dp)
                Text(
                    text = stringResource(R.string.whats_new_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                VSpacer(8.dp)
                CapsualWithText(
                    label = stringResource(R.string.whats_new_version, versionName),
                )
                VSpacer(16.dp)
                HorizontalPager(
                    state = pagerState,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                    pageSpacing = 24.dp,
                ) { page ->
                    WhatsNewChangePage(change = changes[page])
                }
                VSpacer(16.dp)
                PagerDots(pagerState = pagerState)
                VSpacer(24.dp)
                FmPrimaryButton(
                    modifier = Modifier.fillMaxWidth(),
                    text =
                        if (isLastPage) {
                            stringResource(R.string.whats_new_confirm)
                        } else {
                            stringResource(R.string.whats_new_next)
                        },
                    onClick = {
                        if (isLastPage) {
                            onDismiss()
                        } else {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    },
                    isLoading = false,
                    isAffirmed = false,
                )
            }
        }
    }
}

@Composable
private fun WhatsNewChangePage(
    change: WhatsNewChange,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        ChangeTypeBadge(type = change.type)
        VSpacer(12.dp)
        Text(
            text = change.title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        change.description?.let { description ->
            VSpacer(4.dp)
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun ChangeTypeBadge(
    type: ChangeType,
    modifier: Modifier = Modifier,
) {
    val (label, badgeColor) =
        when (type) {
            ChangeType.NEW -> stringResource(R.string.change_type_new) to MaterialTheme.colorScheme.primary
            ChangeType.IMPROVED -> stringResource(R.string.change_type_improved) to MaterialTheme.colorScheme.tertiary
            ChangeType.FIXED -> stringResource(R.string.change_type_fixed) to MaterialTheme.colorScheme.secondary
        }

    Box(
        modifier =
            modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier =
                    Modifier
                        .padding(end = 6.dp)
                        .clip(CircleShape)
                        .background(badgeColor)
                        .padding(4.dp),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PagerDots(
    pagerState: PagerState,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pagerState.pageCount) { page ->
            val isSelected = page == pagerState.currentPage
            val width by animateDpAsState(
                targetValue = if (isSelected) 18.dp else 8.dp,
                animationSpec =
                    spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow,
                    ),
            )
            val color by animateColorAsState(
                if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
            )
            // Squash the dot when it becomes selected and let a bouncy spring
            // wobble it back to full size.
            val wobbleScale = remember { Animatable(1f) }
            LaunchedEffect(isSelected) {
                if (isSelected) {
                    wobbleScale.snapTo(0.5f)
                    wobbleScale.animateTo(
                        targetValue = 1f,
                        animationSpec =
                            spring(
                                dampingRatio = Spring.DampingRatioHighBouncy,
                                stiffness = Spring.StiffnessMedium,
                            ),
                    )
                }
            }
            Box(
                modifier =
                    Modifier
                        .height(8.dp)
                        .width(width)
                        .graphicsLayer {
                            // Stretch the active pill while the user drags the pager;
                            // reading the offset here avoids recomposing every frame.
                            val dragStretch =
                                if (isSelected) {
                                    1f + abs(pagerState.currentPageOffsetFraction) * 0.8f
                                } else {
                                    1f
                                }
                            scaleX = wobbleScale.value * dragStretch
                            scaleY = wobbleScale.value
                        }.clip(CircleShape)
                        .background(color),
            )
        }
    }
}

@Preview
@Composable
private fun WhatsNewDialogPreview() {
    FmWhatsNewDialog(
        versionName = "1.4.0",
        changes =
            listOf(
                WhatsNewChange(
                    type = ChangeType.NEW,
                    title = "Billeder på events",
                    description = "Dine events får nu automatisk et flot billede fra Pexels.",
                ),
                WhatsNewChange(
                    type = ChangeType.IMPROVED,
                    title = "Hurtigere forside",
                    description = "Forsiden indlæses nu markant hurtigere.",
                ),
                WhatsNewChange(
                    type = ChangeType.FIXED,
                    title = "Notifikationer",
                    description = "Rettet en fejl hvor påmindelser ikke blev sendt.",
                ),
            ),
        onDismiss = {},
    )
}
