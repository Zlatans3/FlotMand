package dk.zlatan.flotmand.Features.frontpage.dinner_event

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.lifecycle.HiltViewModel

@Composable
internal fun MyEventScreen(
 modifier: Modifier = Modifier,
 showBackButton: Boolean,
 onBackClick: () -> Unit,
 onTopicClick: (String) -> Unit,
 viewModel : MyEventViewModel = hiltViewModel()
) {
    Column() {

    }
}

@Preview
@Composable
private fun MyEventScreenPreview() {
    MyEventScreen(
        modifier = Modifier,
        showBackButton = true,
        onBackClick = {},
        onTopicClick = {}
    )
}