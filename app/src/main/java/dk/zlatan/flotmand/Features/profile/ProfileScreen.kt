package dk.zlatan.flotmand.Features.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dk.zlatan.flotmand.design_system.componenets.HeaderContainer

@Composable
internal fun ProfileScreen(
 modifier: Modifier = Modifier,

) {
    Column() {
        HeaderContainer(
            modifier = Modifier
        ) {
            // Add profile header content here
        }


    }
}

@Preview
@Composable
private fun ProfileScreenPreview() {
    ProfileScreen(modifier = Modifier)
}