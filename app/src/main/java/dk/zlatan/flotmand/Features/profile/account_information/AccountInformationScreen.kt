package dk.zlatan.flotmand.Features.profile.account_information

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dk.zlatan.flotmand.Features.profile.account_information.ui.AccountDetailsCard
import dk.zlatan.flotmand.Features.profile.account_information.ui.DeleteUserDialog
import dk.zlatan.flotmand.Features.profile.account_information.ui.PersonalInfoCard
import dk.zlatan.flotmand.Features.profile.ui.SettingsGroup
import dk.zlatan.flotmand.Features.profile.ui.SettingsRowItem
import dk.zlatan.flotmand.R
import dk.zlatan.flotmand.design_system.componenets.spacers.VSpacer
import dk.zlatan.flotmand.design_system.componenets.topappbar.FmTopAppBar
import dk.zlatan.flotmand.design_system.theme.FlotMandTheme
import dk.zlatan.flotmand.model.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AccountInformationScreenRoute(
    modifier: Modifier = Modifier,
    viewModel: AccountInformationViewModel = hiltViewModel(),
    onDismiss: () -> Unit,
    onUserDeleted: () -> Unit = onDismiss,
    onOpenLicenses: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val showDeleteDialog = remember { mutableStateOf(false) }
    val isLoading = uiState.isLoading

    var expandDropdownMenu by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            FmTopAppBar(
                textContent = {
                    Text(
                        text = stringResource(R.string.account_information_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                },
                leadingIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.close),
                        )
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
                trailingContent = {
                    IconButton(onClick = { expandDropdownMenu = !expandDropdownMenu }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = stringResource(R.string.show_more_options)
                        )
                    }

                    DropdownMenu(
                        expanded = expandDropdownMenu,
                        onDismissRequest = { expandDropdownMenu = false },
                        containerColor = MaterialTheme.colorScheme.surface,
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(R.string.open_source_licenses_title),
                                )
                            },
                            onClick = {
                                expandDropdownMenu = false
                                onOpenLicenses()
                            }
                        )
                    }
                }
            )
        },
    ) { paddingValues ->
        val topPaddingValues = paddingValues.calculateTopPadding()
        AccountInformationScreenContent(
            modifier =
                modifier
                    .padding(top = topPaddingValues),
            user = uiState.user ?: User(),
            isLoading = isLoading,
            onUpdateDisplayName = { newName -> viewModel.updateDisplayName(newName) },
            onUpdatePhoneNumber = { newPhone -> viewModel.updatePhoneNumber(newPhone) },
            onDeletePhoneNumber = { viewModel.updatePhoneNumber("") },
            onDeleteUserClick = { showDeleteDialog.value = true },
        )
    }

    // Show error message in snackbar
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }
    // Delete user dialog
    if (showDeleteDialog.value) {
        DeleteUserDialog(
            isLoading = isLoading,
            onConfirm = {
                showDeleteDialog.value = false
                viewModel.deleteUser()
                onUserDeleted()
            },
            onDismiss = { showDeleteDialog.value = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountInformationScreenContent(
    modifier: Modifier = Modifier,
    user: User = User(),
    isLoading: Boolean = false,
    onUpdateDisplayName: (String) -> Unit,
    onUpdatePhoneNumber: (String) -> Unit,
    onDeletePhoneNumber: () -> Unit,
    onDeleteUserClick: () -> Unit,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState()),
    ) {
        SectionLabel(stringResource(R.string.personal_info_title))
        PersonalInfoCard(
            modifier = Modifier.padding(horizontal = 20.dp),
            user = user,
            isLoading = isLoading,
            onUpdateDisplayName = onUpdateDisplayName,
            onUpdatePhoneNumber = onUpdatePhoneNumber,
            onDeletePhoneNumber = onDeletePhoneNumber,
        )

        SectionLabel(stringResource(R.string.account_details_title))
        AccountDetailsCard(
            modifier = Modifier.padding(horizontal = 20.dp),
            user = user,
        )

        Spacer(modifier = Modifier.weight(1f))

        VSpacer(28.dp)
        // Destructive action styled like the logout row on the profile screen.
        SettingsGroup(
            items =
                listOf(
                    SettingsRowItem(
                        icon = Icons.Rounded.DeleteForever,
                        title = stringResource(R.string.delete_user_button_title),
                        onClick = onDeleteUserClick,
                        isDestructive = true,
                    ),
                ),
        )
        VSpacer(16.dp)
    }
}

@Composable
private fun SectionLabel(text: String) {
    VSpacer(28.dp)
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 24.dp),
    )
    VSpacer(8.dp)
}

@Preview
@Composable
private fun AccountInformationScreenPreview() {
    FlotMandTheme {
        AccountInformationScreenContent(
            modifier = Modifier,
            user =
                User(
                    id = "123456789",
                    email = "user@example.com",
                    phoneNumber = "+45 12 34 56 78",
                    provider = "Google",
                    displayName = "Oliver Payne",
                    isAnonymous = false,
                ),
            isLoading = false,
            onUpdateDisplayName = {},
            onUpdatePhoneNumber = {},
            onDeletePhoneNumber = {},
            onDeleteUserClick = {},
        )
    }
}
