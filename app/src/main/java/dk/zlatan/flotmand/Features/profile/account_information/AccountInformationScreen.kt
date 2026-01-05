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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
    val user by viewModel.user.collectAsState(initial = null)
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    AccountInformationScreenContent(
        modifier = modifier,
        user = user ?: User(),
        isLoading = isLoading,
        errorMessage = errorMessage,
        onDismiss = onDismiss,
        onUpdateDisplayName = { newName ->
            viewModel.updateDisplayName(newName)
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
    onDismiss: () -> Unit = {},
    onUpdateDisplayName: (String) -> Unit = {},
    onClearError: () -> Unit = {}
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
            EditableInfoCard(
                title = "Personlige Oplysninger",
                items = buildList {
                    if (user.displayName.isNotEmpty() || isEditingDisplayName) {
                        add(
                            EditableInfoItem(
                                label = "Navn",
                                value = user.displayName,
                                isEditing = isEditingDisplayName,
                                editedValue = editedDisplayName,
                                onEditClick = { isEditingDisplayName = true },
                                onValueChange = { editedDisplayName = it },
                                onSave = {
                                    onUpdateDisplayName(editedDisplayName)
                                    isEditingDisplayName = false
                                },
                                onCancel = {
                                    editedDisplayName = user.displayName
                                    isEditingDisplayName = false
                                }
                            )
                        )
                    }
                    if (user.email.isNotEmpty()) {
                        add(
                            EditableInfoItem(
                                label = "Email",
                                value = user.email,
                                isEditing = false,
                                canEdit = false
                            )
                        )
                    }
                    add(
                        EditableInfoItem(
                            label = "Telefon",
                            value = user.phoneNumber,
                            isEditing = isEditingPhoneNumber,
                            editedValue = editedPhoneNumber,
                            onEditClick = { isEditingPhoneNumber = true },
                            onValueChange = { editedPhoneNumber = it },
                            onSave = {
                                // Phone number update would require Firebase phone auth
                                // For now, just show a message
                                snackbarHostState.currentSnackbarData?.dismiss()
                                isEditingPhoneNumber = false
                            },
                            onCancel = {
                                editedPhoneNumber = user.phoneNumber
                                isEditingPhoneNumber = false
                            },
                            placeholder = "Tilføj telefonnummer"
                        )
                    )
                },
                isLoading = isLoading
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

@Composable
private fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        VSpacer(4.dp)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private data class InfoItem(
    val label: String,
    val value: String
)

private data class EditableInfoItem(
    val label: String,
    val value: String,
    val isEditing: Boolean = false,
    val editedValue: String = "",
    val onEditClick: () -> Unit = {},
    val onValueChange: (String) -> Unit = {},
    val onSave: () -> Unit = {},
    val onCancel: () -> Unit = {},
    val canEdit: Boolean = true,
    val placeholder: String = ""
)

@Composable
private fun EditableInfoCard(
    modifier: Modifier = Modifier,
    title: String,
    items: List<EditableInfoItem>,
    isLoading: Boolean = false
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(4.dp),
                        strokeWidth = 2.dp
                    )
                }
            }
            VSpacer(12.dp)

            items.forEachIndexed { index, item ->
                EditableInfoRow(
                    item = item,
                    isLoading = isLoading
                )
                if (index < items.size - 1) {
                    VSpacer(12.dp)
                }
            }
        }
    }
}

@Composable
private fun EditableInfoRow(
    modifier: Modifier = Modifier,
    item: EditableInfoItem,
    isLoading: Boolean = false
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = item.label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        VSpacer(4.dp)

        if (item.isEditing) {
            OutlinedTextField(
                value = item.editedValue,
                onValueChange = item.onValueChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    if (item.placeholder.isNotEmpty()) {
                        Text(item.placeholder)
                    }
                },
                enabled = !isLoading,
                singleLine = true
            )
            VSpacer(8.dp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = item.onCancel,
                    enabled = !isLoading
                ) {
                    Text("Annuller")
                }
                Button(
                    onClick = item.onSave,
                    enabled = !isLoading,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text("Gem")
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.value.ifEmpty { "Ikke angivet" },
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (item.value.isEmpty()) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    modifier = Modifier.weight(1f)
                )
                if (item.canEdit) {
                    IconButton(
                        onClick = item.onEditClick,
                        enabled = !isLoading,
                        modifier = Modifier.padding(0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Rediger ${item.label}",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
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
            onDismiss = {}
        )
    }
}

