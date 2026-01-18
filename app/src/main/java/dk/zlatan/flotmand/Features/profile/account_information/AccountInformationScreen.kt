package dk.zlatan.flotmand.Features.profile.account_information

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dk.zlatan.flotmand.Features.profile.account_information.ui.AccountDetailsCard
import dk.zlatan.flotmand.Features.profile.account_information.ui.PersonalInfoCard
import dk.zlatan.flotmand.design_system.componenets.spacers.VSpacer
import dk.zlatan.flotmand.design_system.componenets.topappbar.FmTopAppBar
import dk.zlatan.flotmand.design_system.theme.FlotMandTheme
import dk.zlatan.flotmand.model.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AccountInformationScreenRoute(
    modifier: Modifier = Modifier,
    viewModel: AccountInformationViewModel = hiltViewModel(),
    onDismiss: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            FmTopAppBar(
                textContent = {
                    Text(
                        text = "Konto Information",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                },
                leadingIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Luk",
                        )
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
            )
        },
    ) { paddingValues ->
        val topPaddingValues = paddingValues.calculateTopPadding()
        AccountInformationScreenContent(
            modifier =
                modifier
                    .padding(top = topPaddingValues),
            user = uiState.user ?: User(),
            isLoading = uiState.isLoading,
            onUpdateDisplayName = { newName -> viewModel.updateDisplayName(newName) },
            onUpdatePhoneNumber = { newPhone ->
                viewModel.updatePhoneNumber(newPhone)
            },
        )
    }

    val snackbarHostState = remember { SnackbarHostState() }

    // Show error message in snackbar
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
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
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        VSpacer(8.dp)

        // Personal Information Card (Editable)
        PersonalInfoCard(
            user = user,
            isLoading = isLoading,
            onUpdateDisplayName = onUpdateDisplayName,
            onUpdatePhoneNumber = onUpdatePhoneNumber,
        )

        // Account Details Card
        AccountDetailsCard(user = user)

        VSpacer(16.dp)
    }
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
        )
    }
}
