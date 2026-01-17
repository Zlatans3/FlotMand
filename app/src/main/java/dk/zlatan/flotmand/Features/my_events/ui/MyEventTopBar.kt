package dk.zlatan.flotmand.Features.my_events.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dk.zlatan.flotmand.R
import dk.zlatan.flotmand.design_system.componenets.topappbar.FmTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MyEventTopBar(modifier: Modifier = Modifier) {
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
    )
}

@Preview
@Composable
private fun MyEventTopBarPreview() {
    MyEventTopBar(modifier = Modifier)
}
