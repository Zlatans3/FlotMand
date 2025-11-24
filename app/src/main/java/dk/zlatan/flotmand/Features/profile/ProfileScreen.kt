package dk.zlatan.flotmand.Features.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dk.zlatan.flotmand.Features.authentication.login.LoginCard
import dk.zlatan.flotmand.Features.profile.ui.SectionCard
import dk.zlatan.flotmand.design_system.componenets.HeaderContainer
import dk.zlatan.flotmand.design_system.componenets.ProfileImage
import dk.zlatan.flotmand.design_system.componenets.spacers.VSpacer
import dk.zlatan.flotmand.design_system.theme.FlotMandTheme

@Composable
fun ProfileScreenRoute(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    ProfileScreen(
        modifier = modifier,
        userName = viewModel.user.value?.displayName ?: "Flotte mand ikke fudnet",
        onLogoutClicked = { viewModel.signOut() }
    )
}

@Composable
internal fun ProfileScreen(
    modifier: Modifier = Modifier,
    userImage: Int? = null,
    userName: String,
    onLogoutClicked: () -> Unit = {},
    ) {
    Column(modifier = modifier.fillMaxWidth()) {
        HeaderContainer(
            modifier = Modifier
        ) {
            VSpacer(90.dp)
            ProfileImage(
                modifier = Modifier,
                profilePic = userImage,
                profileSize = 100.dp,
                userName = "FM"
            )
            VSpacer(20.dp)
            Text(
                text = "Hej!\n$userName",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            VSpacer(12.dp)
        }

        VSpacer(24.dp)
        SectionCard(
            title = "Log ud",
            onClick = onLogoutClicked

        )

        Spacer(modifier = Modifier.weight(1f))




    }
}

@Preview
@Composable
private fun ProfileScreenPreview() {
    FlotMandTheme() {
        ProfileScreen(
            modifier = Modifier,
            userName = "Oliver Payne"
        )
    }
}