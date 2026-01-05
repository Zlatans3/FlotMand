package dk.zlatan.flotmand.Features.profile.account_information

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dk.zlatan.flotmand.Features.profile.account_information.model.InfoItem
import dk.zlatan.flotmand.Features.profile.account_information.ui.InfoRow
import dk.zlatan.flotmand.Features.profile.account_information.ui.PersonalInfoCard
import dk.zlatan.flotmand.design_system.componenets.spacers.VSpacer
import dk.zlatan.flotmand.design_system.theme.FlotMandTheme
import dk.zlatan.flotmand.model.User
import kotlinx.coroutines.launch

@Composable
internal fun AccountInformationScreenRoute(
    modifier: Modifier = Modifier,
    viewModel: AccountInformationViewModel = hiltViewModel(),
    onDismiss: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AccountInformationScreenContent(
        modifier = modifier,
        user = uiState.user ?: User(),
        isLoading = uiState.isLoading,
        errorMessage = uiState.errorMessage,
        onDismiss = onDismiss,
        onUpdateDisplayName = { newName ->
            viewModel.updateDisplayName(newName)
        },
        onUpdatePhoneNumber = { newPhone ->
            viewModel.updatePhoneNumber(newPhone)
        },
        onClearError = {
            viewModel.clearError()
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun AccountInformationScreenContent(
    modifier: Modifier = Modifier,
    user: User = User(),
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onDismiss: () -> Unit,
    onUpdateDisplayName: (String) -> Unit,
    onUpdatePhoneNumber: (String) -> Unit,
    onClearError: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    var isEditingDisplayName by remember { mutableStateOf(false) }
    var editedDisplayName by remember(user.displayName) { mutableStateOf(user.displayName) }
    var isEditingPhoneNumber by remember { mutableStateOf(false) }
    var editedPhoneNumber by remember(user.phoneNumber) { mutableStateOf(user.phoneNumber) }
    var isUserIdVisible by remember { mutableStateOf(false) }

    // Show error message in snackbar
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            onClearError()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Konto Information",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Luk"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            VSpacer(8.dp)

            // Personal Information Card (Editable)
            PersonalInfoCard(
                user = user,
                isEditingDisplayName = isEditingDisplayName,
                editedDisplayName = editedDisplayName,
                isEditingPhoneNumber = isEditingPhoneNumber,
                editedPhoneNumber = editedPhoneNumber,
                isLoading = isLoading,
                onEditDisplayName = { isEditingDisplayName = true },
                onDisplayNameChange = { editedDisplayName = it },
                onSaveDisplayName = {
                    onUpdateDisplayName(editedDisplayName)
                    isEditingDisplayName = false
                },
                onCancelDisplayName = {
                    editedDisplayName = user.displayName
                    isEditingDisplayName = false
                },
                onEditPhoneNumber = { isEditingPhoneNumber = true },
                onPhoneNumberChange = { editedPhoneNumber = it },
                onSavePhoneNumber = {
                    onUpdatePhoneNumber(editedPhoneNumber)
                    isEditingPhoneNumber = false
                },
                onCancelPhoneNumber = {
                    editedPhoneNumber = user.phoneNumber
                    isEditingPhoneNumber = false
                }
            )

            // Account Details Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Konto Detaljer",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    VSpacer(12.dp)

                    // User ID with visibility toggle
                    if (user.id.isNotEmpty()) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Bruger ID",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                            VSpacer(4.dp)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isUserIdVisible) user.id else "••••••••",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier
                                        .weight(1f)
                                        .combinedClickable(
                                            onClick = { },
                                            onLongClick = {
                                                clipboardManager.setText(AnnotatedString(user.id))
                                                coroutineScope.launch {
                                                    snackbarHostState.showSnackbar("Bruger ID kopieret til udklipsholder")
                                                }
                                            }
                                        )
                                )
                                IconButton(
                                    onClick = { isUserIdVisible = !isUserIdVisible },
                                    modifier = Modifier.padding(0.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isUserIdVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (isUserIdVisible) "Skjul ID" else "Vis ID",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Provider info
                    if (user.provider.isNotEmpty()) {
                        if (user.id.isNotEmpty()) {
                            VSpacer(12.dp)
                        }
                        InfoRow(
                            label = "Log ind med",
                            value = user.provider
                        )
                    }
                }
            }

            VSpacer(16.dp)
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    items: List<InfoItem>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            VSpacer(12.dp)

            items.forEachIndexed { index, item ->
                InfoRow(
                    label = item.label,
                    value = item.value
                )
                if (index < items.size - 1) {
                    VSpacer(12.dp)
                }
            }
        }
    }
}

@Preview
@Composable
private fun AccountInformationScreenPreview() {
    FlotMandTheme {
        AccountInformationScreenContent(
            modifier = Modifier,
            user = User(
                id = "123456789",
                email = "user@example.com",
                phoneNumber = "+45 12 34 56 78",
                provider = "Google",
                displayName = "Oliver Payne",
                isAnonymous = false
            ),
            onDismiss = {},
            isLoading = false,
            errorMessage = null,
            onUpdateDisplayName = {},
            onUpdatePhoneNumber = {},
            onClearError = {},
        )
    }
}

