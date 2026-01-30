package dk.zlatan.flotmand.Features.my_events.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
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
    val onPrimaryContainer = MaterialTheme.colorScheme.onPrimaryContainer
    val unselectedColor = onPrimaryContainer.copy(alpha = 0.7f)
    PrimaryTabRow(
        selectedTabIndex = selectedTabIndex,
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = onPrimaryContainer,
        modifier = modifier,
        indicator = {
            TabRowDefaults.PrimaryIndicator(
                modifier = Modifier
                    .tabIndicatorOffset(selectedTabIndex, matchContentSize = true),
                width = Dp.Unspecified,
                color = MaterialTheme.colorScheme.primary
            )
        }
    ) {
        tabTitles.forEachIndexed { index, title ->
            val isSelected = selectedTabIndex == index
            Tab(
                selected = isSelected,
                onClick = { onTabSelected(index) },
                text = {
                    Text(
                        title,
                        style = if (isSelected)
                            MaterialTheme.typography.titleMedium.copy(
                                color = onPrimaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        else
                            MaterialTheme.typography.bodyMedium.copy(
                                color = unselectedColor,
                                fontWeight = FontWeight.Normal
                            ),
                        modifier = Modifier.padding(vertical = 6.dp, horizontal = 12.dp)
                    )
                },
                selectedContentColor = onPrimaryContainer,
                unselectedContentColor = unselectedColor,
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
