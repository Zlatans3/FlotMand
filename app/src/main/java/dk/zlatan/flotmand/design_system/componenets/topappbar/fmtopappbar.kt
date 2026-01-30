package dk.zlatan.flotmand.design_system.componenets.topappbar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * Created to make sure all TopAppBars in the app consistently use the same top app bar style.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FmTopAppBar(
    modifier: Modifier = Modifier,
    inserts: WindowInsets = TopAppBarDefaults.windowInsets,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    leadingIcon: @Composable () -> Unit = {},
    textContent: @Composable () -> Unit = {},
    trailingContent: @Composable () -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    Column(modifier = modifier) {
        TopAppBar(
            title = {
                textContent()
            },
            navigationIcon = {
                leadingIcon()
            },
            actions = { trailingContent() },
            windowInsets = inserts,
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = containerColor,
                ),
            scrollBehavior = scrollBehavior,
        )
    }
}
