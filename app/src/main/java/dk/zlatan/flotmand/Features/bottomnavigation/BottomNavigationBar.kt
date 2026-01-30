package dk.zlatan.flotmand.Features.bottomnavigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun FmBottomNavigationBar(
    currentTab: TopLevelDestination,
    selectedTabIconColor: Color,
    onBottomNavigationClicked: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(
        contentColor = MaterialTheme.colorScheme.onSurface,
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier,
    ) {
        TopLevelDestination.entries.forEach { menuItem ->
            val isSelected = menuItem == currentTab

            val selectedColor =
                if (isSelected) {
                    selectedTabIconColor
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            NavigationBarItem(
                modifier = Modifier,
                icon = {
                    BottomNavigationIcon(
                        item = menuItem,
                        color = selectedColor,
                    )
                },
                label = {
                    Text(
                        text = stringResource(menuItem.iconTextId),
                        style = MaterialTheme.typography.labelMedium,
                        color = selectedColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                selected = isSelected,
                onClick = { onBottomNavigationClicked(menuItem) },
            )
        }
    }
}

@Composable
private fun BottomNavigationIcon(
    item: TopLevelDestination,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .width(56.dp)
                .padding(vertical = 3.dp),
    ) {
        Icon(
            contentDescription = stringResource(item.iconTextId),
            tint = color,
            imageVector = item.unselectedIcon,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

@Preview
@Composable
private fun FmBottomNavigationBarPreview() {
    FmBottomNavigationBar(
        currentTab = TopLevelDestination.HOME,
        selectedTabIconColor = MaterialTheme.colorScheme.primary,
        onBottomNavigationClicked = {},
    )
}
