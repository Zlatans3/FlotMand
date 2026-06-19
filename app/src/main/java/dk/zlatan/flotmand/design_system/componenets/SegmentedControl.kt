package dk.zlatan.flotmand.design_system.componenets

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dk.zlatan.flotmand.design_system.theme.FlotMandTheme

/** Inset between the outer track and the sliding pill on all sides. */
private val PillInset = 4.dp

/** Minimum touch-target height (48 dp per accessibility guidelines). */
private val MinTouchTargetHeight = 48.dp

/**
 * A full-width, capsule-shaped segmented control with a smooth spring-animated
 * pill indicator.
 *
 * @param selectedIndex    Zero-based index of the active segment.
 * @param options          Segment labels; must contain at least one entry.
 * @param onOptionSelected Invoked with the new i/** Inset between the outer track and the sliding pill on all sides. */ taps a segment.
 */
@Composable
fun SegmentedControl(
    selectedIndex: Int,
    options: List<String>,
    onOptionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    require(options.isNotEmpty()) { "SegmentedControl requires at least one option." }

    val trackColor = MaterialTheme.colorScheme.inverseOnSurface
    val pillColor = MaterialTheme.colorScheme.primary
    val activeTextColor = MaterialTheme.colorScheme.onPrimary
    val inactiveTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.50f)

    // One stable interaction source per segment, keyed by list size.
    // indication = null suppresses ripple — the pill slide is the visual feedback.
    val interactionSources = remember(options.size) {
        List(options.size) { MutableInteractionSource() }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(MinTouchTargetHeight)
            .background(trackColor, CircleShape)
            .clip(CircleShape),
    ) {
        val trackWidth = maxWidth - PillInset * 2
        val pillWidth = trackWidth / options.size

        val pillOffsetX by animateDpAsState(
            targetValue = PillInset + pillWidth * selectedIndex,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow,
            ),
            label = "segmentedControlPillOffset",
        )

        // Layer 1: sliding pill background
        Box(
            modifier = Modifier
                .offset(x = pillOffsetX, y = PillInset)
                .width(pillWidth)
                .height(MinTouchTargetHeight - PillInset * 2)
                .background(pillColor, CircleShape),
        )

        // Layer 2: labels drawn on top of the pill
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
        ) {
            options.forEachIndexed { index, label ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = interactionSources[index],
                            indication = null,
                            onClick = { onOptionSelected(index) },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (index == selectedIndex) activeTextColor else inactiveTextColor,
                    )
                }
            }
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────
// All previews use FlotMandTheme so the actual app colours (blue / teal palette)
// are rendered rather than the default Material baseline theme.
// Background colour 0xFF0097A7 mimics the secondaryContainer teal used by MyEventTopBar.

@Preview(
    name = "Control on teal header — Upcoming selected",
    showBackground = true,
    backgroundColor = 0xFF0097A7,
)
@Composable
private fun SegmentedControlUpcomingPreview() {
    FlotMandTheme(darkTheme = true, dynamicColor = false) {
        SegmentedControl(
            selectedIndex = 0,
            options = listOf("Kommende", "Tidligere"),
            onOptionSelected = {},
        )
    }
}

@Preview(
    name = "Control on teal header — Past selected",
    showBackground = true,
    backgroundColor = 0xFF0097A7,
)
@Composable
private fun SegmentedControlPastPreview() {
    FlotMandTheme(darkTheme = true, dynamicColor = false) {
        SegmentedControl(
            selectedIndex = 1,
            options = listOf("Kommende", "Tidligere"),
            onOptionSelected = {},
        )
    }
}

@Preview(
    name = "Control on light header — Upcoming selected",
    showBackground = true,
    backgroundColor = 0xFF4DD0E1,
)
@Composable
private fun SegmentedControlLightPreview() {
    FlotMandTheme(darkTheme = false, dynamicColor = false) {
        SegmentedControl(
            selectedIndex = 0,
            options = listOf("Kommende", "Tidligere"),
            onOptionSelected = {},
        )
    }
}

@Preview(
    name = "Control — interactive",
    showBackground = true,
    backgroundColor = 0xFF0097A7,
)
@Composable
private fun SegmentedControlInteractivePreview() {
    var selected by remember { mutableIntStateOf(0) }
    FlotMandTheme(darkTheme = true, dynamicColor = false) {
        SegmentedControl(
            selectedIndex = selected,
            options = listOf("Kommende", "Tidligere"),
            onOptionSelected = { selected = it },
        )
    }
}
