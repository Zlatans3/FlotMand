package dk.zlatan.flotmand.Features.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dk.zlatan.flotmand.R
import dk.zlatan.flotmand.design_system.componenets.ProfileImage
import dk.zlatan.flotmand.design_system.componenets.spacers.VSpacer
import dk.zlatan.flotmand.design_system.theme.FlotMandTheme
import dk.zlatan.flotmand.model.User

@Composable
fun ProfileSetupScreenRoute(
    modifier: Modifier = Modifier,
    viewModel: ProfileSetupViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ProfileSetupScreen(
        modifier = modifier,
        uiState = uiState,
        onDisplayNameChange = viewModel::onDisplayNameChange,
        onPhoneNumberChange = viewModel::onPhoneNumberChange,
        onBioChange = viewModel::onBioChange,
        onContinue = viewModel::saveAndContinue,
        onSkip = viewModel::skip,
    )
}

@Composable
internal fun ProfileSetupScreen(
    modifier: Modifier = Modifier,
    uiState: ProfileSetupUiState,
    onDisplayNameChange: (String) -> Unit = {},
    onPhoneNumberChange: (String) -> Unit = {},
    onBioChange: (String) -> Unit = {},
    onContinue: () -> Unit = {},
    onSkip: () -> Unit = {},
) {
    val backgroundBrush = Brush.verticalGradient(
        0f to MaterialTheme.colorScheme.primaryContainer,
        0.4f to MaterialTheme.colorScheme.background,
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .systemBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        VSpacer(32.dp)

        ProfileImage(
            profilePic = uiState.photoUrl,
            userName = uiState.displayName,
            profileSize = 96.dp,
        )

        VSpacer(16.dp)

        Text(
            text = stringResource(R.string.profile_setup_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )

        VSpacer(8.dp)

        Text(
            text = stringResource(R.string.profile_setup_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        VSpacer(24.dp)

        OutlinedTextField(
            value = uiState.displayName,
            onValueChange = onDisplayNameChange,
            label = { Text(stringResource(R.string.profile_setup_name_label)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        VSpacer(12.dp)

        OutlinedTextField(
            value = uiState.phoneNumber,
            onValueChange = onPhoneNumberChange,
            label = { Text(stringResource(R.string.profile_setup_phone_label)) },
            supportingText = { Text(stringResource(R.string.profile_setup_phone_helper)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        VSpacer(12.dp)

        OutlinedTextField(
            value = uiState.bio,
            onValueChange = onBioChange,
            label = { Text(stringResource(R.string.profile_setup_bio_label)) },
            placeholder = { Text(stringResource(R.string.user_details_bio_placeholder)) },
            supportingText = {
                Text(
                    text =
                        stringResource(
                            R.string.user_details_bio_char_count,
                            uiState.bio.length,
                            User.BIO_MAX_LENGTH,
                        ),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End,
                )
            },
            minLines = 3,
            modifier = Modifier.fillMaxWidth(),
        )

        uiState.errorMessage?.let { message ->
            VSpacer(12.dp)
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
            )
        }

        VSpacer(24.dp)

        Button(
            onClick = onContinue,
            enabled = !uiState.isSaving && uiState.displayName.isNotBlank(),
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (uiState.isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
            } else {
                Text(
                    text = stringResource(R.string.profile_setup_continue),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 4.dp),
                )
            }
        }

        VSpacer(4.dp)

        TextButton(
            onClick = onSkip,
            enabled = !uiState.isSaving,
        ) {
            Text(stringResource(R.string.profile_setup_skip))
        }

        VSpacer(16.dp)
    }
}

@PreviewLightDark
@Composable
private fun ProfileSetupScreenPreview() {
    FlotMandTheme {
        ProfileSetupScreen(
            uiState =
                ProfileSetupUiState(
                    displayName = "Zlatan Stadler",
                ),
        )
    }
}
