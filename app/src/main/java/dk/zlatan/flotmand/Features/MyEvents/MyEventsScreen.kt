package dk.zlatan.flotmand.Features.MyEvents

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
internal fun MyEventsScreenRoute(
    modifier: Modifier = Modifier,
    viewmodel: MyEventsViewmodel = hiltViewModel(),
    ) {
    MyEventsScreenContent(
        modifier = modifier,
        onClickable = {  }
    )
}

@Composable
private fun MyEventsScreenContent(
    modifier: Modifier = Modifier,
    onClickable: () -> Unit,
) {

}

@Preview
@Composable
private fun MyEventsScreenPreview() {
    MyEventsScreenContent(
        modifier = Modifier,
        onClickable = { }
    )
}