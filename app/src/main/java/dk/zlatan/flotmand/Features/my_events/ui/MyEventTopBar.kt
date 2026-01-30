package dk.zlatan.flotmand.Features.my_events.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dk.zlatan.flotmand.R
import dk.zlatan.flotmand.design_system.componenets.topappbar.FmTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MyEventTopBar(
    modifier: Modifier = Modifier,
    onFilterClick: (() -> Unit)? = null,
    filterMenuContent: @Composable (() -> Unit)? = null
) {
    FmTopAppBar(
        modifier = modifier,
        textContent = {
            Text(
                stringResource(R.string.my_event_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier =
                    Modifier
                        .padding(start = 16.dp, bottom = 8.dp)
                        .statusBarsPadding(),
            )
        },
        trailingContent = {
            if (onFilterClick != null) {
                IconButton(onClick = onFilterClick) {
                    Icon(Icons.Filled.FilterList, contentDescription = stringResource(R.string.filter))
                }
            }
            filterMenuContent?.invoke()
        }
    )
}

@Composable
fun EventTabBar(
    tabTitles: List<String>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    TabRow(
        selectedTabIndex = selectedTabIndex,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.error,
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                color = MaterialTheme.colorScheme.primary
            )
        },
        modifier = modifier
    ) {
        tabTitles.forEachIndexed { index, title ->
            Tab(
                selected = selectedTabIndex == index,
                onClick = { onTabSelected(index) },
                text = {
                    Text(
                        title,
                        color = if (selectedTabIndex == index)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
        }
    }
}

@Preview
@Composable
private fun MyEventTopBarPreview() {
    MyEventTopBar(modifier = Modifier)
}



@Preview(showBackground = true, name = "EventTabBar Preview")
@Composable
private fun EventTabBarPreview() {
    var selectedTab by rememberSaveable { mutableStateOf(0) }
    val tabTitles = listOf("Kommende", "Tidligere")
    Column {
        MyEventTopBar(modifier = Modifier)
        EventTabBar(
            tabTitles = tabTitles,
            selectedTabIndex = selectedTab,
            onTabSelected = { selectedTab = it },
            modifier = Modifier,
        )
    }
}
