package dk.zlatan.flotmand.Features.my_events.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dk.zlatan.flotmand.R
import dk.zlatan.flotmand.design_system.componenets.SegmentedControl
import dk.zlatan.flotmand.design_system.theme.FlotMandTheme

/**
 * Top bar for the My Events screen.
 *
 * Renders the screen title and the [SegmentedControl] tab switcher inside a
 * single cohesive header surface.  Using a custom [Surface] + [Column] layout
 * (rather than the standard [FmTopAppBar]) lets us avoid the fixed 64 dp
 * height constraint of Material3's [TopAppBar] and freely stack the title
 * above the pill switch.
 *
 * @param selectedTab    Currently active tab index (0 = Upcoming, 1 = Past).
 * @param onTabSelected  Callback invoked when the user taps a tab.
 * @param modifier       Applied to the outer [Surface] container.
 */
@Composable
internal fun MyEventTopBar(
    selectedTab: Int = 0,
    onTabSelected: (Int) -> Unit = {},
    modifier: Modifier = Modifier,
    isSelectionMode: Boolean = false,
    selectedCount: Int = 0,
    totalCount: Int = 0,
    onDeleteSelected: () -> Unit = {},
    onToggleSelectAll: () -> Unit = {},
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.secondaryContainer,
        shadowElevation = 4.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.my_event_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(12.dp))

            AnimatedContent(
                targetState = isSelectionMode,
                transitionSpec = {
                    if (targetState) {
                        slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
                    } else {
                        slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                    }
                },
                label = "topBarMode",
            ) { selectionMode ->
                if (selectionMode) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        IconButton(onClick = onToggleSelectAll) {
                            Icon(
                                imageVector = if (selectedCount == totalCount) Icons.Filled.CheckBox else Icons.Filled.CheckBoxOutlineBlank,
                                contentDescription = "Vælg alle",
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                        Text(
                            text = "$selectedCount valgt",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(
                            onClick = onDeleteSelected,
                            enabled = selectedCount > 0,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Slet valgte",
                                tint = if (selectedCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                } else {
                    SegmentedControl(
                        selectedIndex = selectedTab,
                        options = listOf(
                            stringResource(R.string.tab_upcoming),
                            stringResource(R.string.tab_past),
                        ),
                        onOptionSelected = onTabSelected,
                    )
                }
            }
        }
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview(name = "TopBar — Upcoming selected")
@Composable
private fun MyEventTopBarUpcomingPreview() {
    FlotMandTheme(dynamicColor = true) {
        MyEventTopBar(selectedTab = 0)
    }
}

@Preview(name = "TopBar — Past selected")
@Composable
private fun MyEventTopBarPastPreview() {
    FlotMandTheme(dynamicColor = true) {
        MyEventTopBar(selectedTab = 1)
    }
}

@Preview(name = "TopBar — interactive")
@Composable
private fun MyEventTopBarInteractivePreview() {
    var selected by remember { mutableIntStateOf(0) }
    FlotMandTheme(dynamicColor = true) {
        MyEventTopBar(
            selectedTab = selected,
            onTabSelected = { selected = it },
        )
    }
}
