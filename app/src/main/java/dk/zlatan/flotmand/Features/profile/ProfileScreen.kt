package dk.zlatan.flotmand.Features.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import dk.zlatan.flotmand.util.UCropContract
import android.net.Uri
import java.io.File
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dk.zlatan.flotmand.BuildConfig
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
    val isUploadingPhoto by viewModel.isUploadingPhoto.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    var sinOutDialogState by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Photo picker → uCrop → upload. Launchers are declared in reverse order because
    // each one references the next, so the upload step must be registered first.
    val uCropLauncher = rememberLauncherForActivityResult(UCropContract()) { uri ->
        uri?.let { viewModel.updateProfilePhoto(it) }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            val destination = Uri.fromFile(File(context.cacheDir, "profile_photo_crop.jpg"))
            uCropLauncher.launch(Pair(it, destination))
        }
    }

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
            isUploadingPhoto = isUploadingPhoto,
            eventsHosted = uiState.eventsHosted,
            eventsAttended = uiState.eventsAttended,
            upcomingEvents = uiState.upcomingEvents,
            onProfileImageClick = {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
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
    isUploadingPhoto: Boolean = false,
    eventsHosted: Int = 0,
    eventsAttended: Int = 0,
    upcomingEvents: Int = 0,
    onProfileImageClick: () -> Unit = {},
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
                Box(contentAlignment = Alignment.BottomEnd) {
                    ProfileImage(
                        profilePic = userImage,
                        profileSize = 80.dp,
                        userName = userName,
                        onClick = onProfileImageClick,
                    )
                    if (isUploadingPhoto) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(80.dp),
                            strokeWidth = 3.dp,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = stringResource(R.string.change_profile_photo),
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
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
