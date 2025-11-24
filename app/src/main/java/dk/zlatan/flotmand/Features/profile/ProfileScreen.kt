package dk.zlatan.flotmand.Features.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dk.zlatan.flotmand.R
import dk.zlatan.flotmand.design_system.componenets.HeaderContainer
import dk.zlatan.flotmand.design_system.componenets.ProfileImage
import dk.zlatan.flotmand.design_system.componenets.spacers.VSpacer

@Composable
internal fun ProfileScreen(
    modifier: Modifier = Modifier,
    userImage: Int? = null,
    userName: String = "Gustav"
    ) {
    Column() {
        HeaderContainer(
            modifier = Modifier
        ) {
            VSpacer(90.dp)
            ProfileImage(
                modifier = Modifier,
                profilePic = userImage,
                userNameInitials = "FM"
            )
            VSpacer(12.dp)
            Text(
                text = "Hej! $userName",
            )
        }


    }
}

@Preview
@Composable
private fun ProfileScreenPreview() {
    ProfileScreen(
        modifier = Modifier,


    )
}