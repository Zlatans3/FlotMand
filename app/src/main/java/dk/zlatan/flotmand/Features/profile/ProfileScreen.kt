package dk.zlatan.flotmand.Features.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import dk.zlatan.flotmand.BuildConfig
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dk.zlatan.flotmand.Features.profile.ui.DisplayName
import dk.zlatan.flotmand.Features.profile.ui.ProfileStatsRow
import dk.zlatan.flotmand.Features.profile.ui.SectionCard
import dk.zlatan.flotmand.R
import dk.zlatan.flotmand.design_system.componenets.HeaderContainer
import dk.zlatan.flotmand.design_system.componenets.ProfileImage
import dk.zlatan.flotmand.design_system.componenets.dialogs.FmAlertDialog
import dk.zlatan.flotmand.design_system.componenets.spacers.VSpacer
import dk.zlatan.flotmand.design_system.componenets.topappbar.FmTopAppBar
import dk.zlatan.flotmand.design_system.icon.FmIcons
import dk.zlatan.flotmand.design_system.theme.FlotMandTheme
import dk.zlatan.flotmand.model.User

// TODO: Zlatan 22/01/2026 Make a navigation destination class
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreenRoute(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
    navigateToLogin: () -> Unit = {},
    onLanguageClick: () -> Unit = {},
    onNotificationSettingsClick: () -> Unit = {},
    navigateToAccountInformation: () -> Unit = {},
    onThemeSettingsClick: () -> Unit = {},
    onPrivacyPolicyClick: () -> Unit = {},
) {
    val user by viewModel.user.collectAsState(initial = User())
    val isLoading by viewModel.signOutLoading.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    var sinOutDialogState by remember { mutableStateOf(false) }
    Scaffold(
        modifier = modifier,
        topBar = {
            FmTopAppBar(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        },
    ) { paddingValues ->
        val topPaddingValues = paddingValues.calculateTopPadding()
        ProfileScreen(
            modifier =
                modifier
                    .padding(top = topPaddingValues)
                    .fillMaxSize(),
            userName = user.displayName,
            userImage = user.photoUrl,
            eventsHosted = uiState.eventsHosted,
            eventsAttended = uiState.eventsAttended,
            upcomingEvents = uiState.upcomingEvents,
            onLogoutClicked = {
                sinOutDialogState = true
            },
            onAccountInformationClick = navigateToAccountInformation,
            onLanguageClick = onLanguageClick,
            onNotificationSettingsClick = onNotificationSettingsClick,
            onThemeSettingsClick = onThemeSettingsClick,
            onPrivacyPolicyClick = onPrivacyPolicyClick,
        )
    }

    if (sinOutDialogState) {
        FmAlertDialog(
            isLoading = isLoading,
            onDismiss = {
                sinOutDialogState = false
            },
            onActionClick = {
                viewModel.signOut()
                sinOutDialogState = false
                navigateToLogin()
            },
        )
    }
}

@Composable
internal fun ProfileScreen(
    modifier: Modifier = Modifier,
    userImage: String? = null,
    userName: String,
    eventsHosted: Int = 0,
    eventsAttended: Int = 0,
    upcomingEvents: Int = 0,
    onLogoutClicked: () -> Unit = {},
    onAccountInformationClick: () -> Unit = {},
    onLanguageClick: () -> Unit,
    onNotificationSettingsClick: () -> Unit = {},
    onThemeSettingsClick: () -> Unit = {},
    onPrivacyPolicyClick: () -> Unit = {},
) {
    LazyColumn(
        modifier =
            modifier
                .background(MaterialTheme.colorScheme.background),
    ) {
        item {
            HeaderContainer(
                modifier = Modifier,
                color = MaterialTheme.colorScheme.secondaryContainer,
            ) {
                VSpacer(60.dp)
                ProfileImage(
                    modifier = Modifier,
                    profilePic = userImage,
                    profileSize = 80.dp,
                    userName = userName,
                )
                VSpacer(8.dp)
                DisplayName(displayName = userName)
                VSpacer(16.dp)
                ProfileStatsRow(
                    eventsHosted = eventsHosted,
                    eventsAttended = eventsAttended,
                    upcomingEvents = upcomingEvents,
                )
                VSpacer(20.dp)
            }
        }
        item {
            VSpacer(24.dp)
            Text(
                text = stringResource(R.string.profile_section_settings),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
            VSpacer(12.dp)
            SectionCard(
                title = stringResource(R.string.language_screen_title),
                iconRes = FmIcons.globe,
                onClick = onLanguageClick,
            )
            VSpacer(12.dp)
            SectionCard(
                title = stringResource(R.string.theme_settings_title),
                iconRes = FmIcons.darkMode,
                onClick = onThemeSettingsClick,
            )
            VSpacer(12.dp)
            SectionCard(
                title = stringResource(R.string.notification_settings_title),
                iconRes = FmIcons.Bell,
                onClick = onNotificationSettingsClick,
            )

            VSpacer(50.dp)
            Text(
                text = stringResource(R.string.profile_section_other),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
            VSpacer(12.dp)
            SectionCard(
                title = stringResource(R.string.privacy_policy),
                iconRes = FmIcons.privacyTip,
                trailingIcon = FmIcons.openInNew,
                onClick = onPrivacyPolicyClick,
            )

            VSpacer(50.dp)
            Text(
                text = stringResource(R.string.profile_section_user),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
            VSpacer(12.dp)
            SectionCard(
                title = stringResource(R.string.account_information_title),
                iconRes = FmIcons.Person,
                onClick = onAccountInformationClick,
            )
            VSpacer(12.dp)
            SectionCard(
                title = stringResource(R.string.logout),
                iconRes = FmIcons.logout,
                onClick = onLogoutClicked,
            )
            VSpacer(24.dp)
            Text(
                text = "v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
            )
        }
    }
}

@Preview
@Composable
private fun ProfileScreenPreview() {
    FlotMandTheme {
        ProfileScreen(
            modifier = Modifier,
            userName = "Oliver Payne",
            eventsHosted = 12,
            eventsAttended = 8,
            upcomingEvents = 3,
            onLanguageClick = {},
        )
    }
}
