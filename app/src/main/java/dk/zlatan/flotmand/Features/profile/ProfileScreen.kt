package dk.zlatan.flotmand.Features.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dk.zlatan.flotmand.Features.profile.ui.DisplayName
import dk.zlatan.flotmand.Features.profile.ui.SectionCard
import dk.zlatan.flotmand.design_system.componenets.HeaderContainer
import dk.zlatan.flotmand.design_system.componenets.ProfileImage
import dk.zlatan.flotmand.design_system.componenets.dialogs.FmAlertDialog
import dk.zlatan.flotmand.design_system.componenets.spacers.VSpacer
import dk.zlatan.flotmand.design_system.icon.FmIcons
import dk.zlatan.flotmand.design_system.theme.FlotMandTheme
import dk.zlatan.flotmand.model.User

private const val set_name = "opsæt navn"

@Composable
fun ProfileScreenRoute(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
    navigateToLogin: () -> Unit = {},
) {
    val user by viewModel.user.collectAsState(initial = User(displayName = set_name))
    val signedOut by viewModel.signedOut.collectAsState()
    val isLoading by viewModel.signOutLoading.collectAsState()
    var sinOutDialogState by remember { mutableStateOf(false) }
    var newDisplayName by remember { mutableStateOf(user.displayName) }

    // Navigate to login when signed out
    if (signedOut) {
        LaunchedEffect(signedOut) {
            navigateToLogin()
        }
    }

    ProfileScreen(
        modifier = modifier.fillMaxSize(),
        userName = user.displayName,
        userImage = user.photoUrl,
        onLogoutClicked = {
            sinOutDialogState = true
        },
        onUpdateDisplayNameClick = { newName ->
            viewModel.onUpdateDisplayNameClick(newName)
        }
    )

    if (sinOutDialogState)
        FmAlertDialog(
            isLoading = isLoading,
            onDismiss = {
                sinOutDialogState = false
            },
            onSignOutClick = {
                viewModel.signOut()
                sinOutDialogState = false
                navigateToLogin()
            }
        )
}

@Composable
internal fun ProfileScreen(
    modifier: Modifier = Modifier,
    userImage: String? = null,
    userName: String,
    onUpdateDisplayNameClick: (String) -> Unit,
    onLogoutClicked: () -> Unit = {},
) {
    LazyColumn(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceContainer),
    ) {
        item {
            HeaderContainer(
                modifier = Modifier
            ) {
                VSpacer(90.dp)
                ProfileImage(
                    modifier = Modifier,
                    profilePic = userImage,
                    profileSize = 100.dp,
                    userName = userName
                )
                VSpacer(20.dp)
                DisplayName(
                    displayName = userName,
                    isLoading = false,
                    onUpdateDisplayNameClick = { updatedName ->
                        onUpdateDisplayNameClick(updatedName)
                    }
                )
                VSpacer(12.dp)
            }
        }
        item {
            VSpacer(24.dp)
            SectionCard(
                title = "Konto Information",
                iconRes = FmIcons.Person,
                onClick = {  }
            )
            VSpacer(12.dp)
            SectionCard(
                title = "Log ud",
                iconRes = FmIcons.logout,
                onClick = onLogoutClicked
            )
            VSpacer(24.dp)
        }
    }
}

@Preview
@Composable
private fun ProfileScreenPreview() {
    FlotMandTheme() {
        ProfileScreen(
            modifier = Modifier,
            userName = "Oliver Payne",
            onUpdateDisplayNameClick = {},
        )
    }
}