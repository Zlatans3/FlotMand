@file:Suppress(
    "ktlint:standard:function-naming",
    "ktlint:standard:package-name",
    "ktlint:standard:max-line-length",
    "ktlint:standard:import-ordering",
)

package dk.zlatan.flotmand.Features.frontpage.host_rotation

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.ui.tooling.preview.Preview
import dk.zlatan.flotmand.design_system.theme.FlotMandTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dk.zlatan.flotmand.R
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dk.zlatan.flotmand.design_system.componenets.ProfileImage
import dk.zlatan.flotmand.design_system.componenets.spacers.HSpacer
import dk.zlatan.flotmand.model.User
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HostRotationSheet(
    onDismiss: () -> Unit,
    snackbarHostState: SnackbarHostState,
    viewModel: HostRotationViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    LaunchedEffect(uiState.errorMessage) {
        if (uiState.errorMessage != null) {
            snackbarHostState.showSnackbar(uiState.errorMessage!!)
            viewModel.onErrorDismissed()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        HostRotationSheetContent(
            rotationMembers = uiState.rotationMembers,
            showAddGhostUserDialog = uiState.showAddGhostUserDialog,
            onAddGhostUserClick = viewModel::onShowAddGhostUserDialog,
            onDismissDialog = viewModel::onDismissDialog,
            onConfirmAddGhostUser = viewModel::onAddGhostUser,
            onRemoveUser = viewModel::onRemoveUser,
            onSaveOrder = viewModel::onSaveOrder,
            onResetPlacements = {
                viewModel.onResetPlacements()
                onDismiss()
            },
        )
    }
}

@Composable
private fun HostRotationSheetContent(
    rotationMembers: List<User>,
    showAddGhostUserDialog: Boolean,
    onAddGhostUserClick: () -> Unit,
    onDismissDialog: () -> Unit,
    onConfirmAddGhostUser: (String) -> Unit,
    onRemoveUser: (String) -> Unit,
    onSaveOrder: (List<String>) -> Unit,
    onResetPlacements: () -> Unit,
) {
    var displayMembers by remember { mutableStateOf(rotationMembers) }

    LaunchedEffect(rotationMembers) {
        displayMembers = rotationMembers
    }

    val lazyListState = rememberLazyListState()
    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
        displayMembers = displayMembers.toMutableList().apply {
            add(to.index, removeAt(from.index))
        }
    }

    Column(modifier = Modifier.padding(start = 20.dp, end = 4.dp, bottom = 12.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = stringResource(R.string.rotation_order_title),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onAddGhostUserClick) {
                Icon(
                    imageVector = Icons.Filled.PersonAdd,
                    contentDescription = stringResource(R.string.add_user_content_description),
                )
            }
        }
        Text(
            text = stringResource(R.string.rotation_reorder_hint),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 16.dp),
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(displayMembers, key = { it.id }) { user ->
                ReorderableItem(reorderableState, key = user.id) { isDragging ->
                    val elevation by animateDpAsState(if (isDragging) 8.dp else 0.dp)
                    RotationMemberRow(
                        user = user,
                        shadowElevation = elevation,
                        onRemove = { onRemoveUser(user.id) },
                        dragHandle = {
                            IconButton(
                                modifier = Modifier.draggableHandle(
                                    onDragStopped = {
                                        onSaveOrder(displayMembers.map { it.id })
                                    },
                                ),
                                onClick = {},
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.DragHandle,
                                    contentDescription = stringResource(R.string.drag_handle_content_description),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        },
                    )
                }
            }

            item {
                OutlinedButton(
                    onClick = onResetPlacements,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .navigationBarsPadding(),
                ) {
                    Text(stringResource(R.string.reset_placements))
                }
            }
        }
    }

    if (showAddGhostUserDialog) {
        AddGhostUserDialog(
            onDismiss = onDismissDialog,
            onConfirm = onConfirmAddGhostUser,
        )
    }
}

@Composable
private fun RotationMemberRow(
    user: User,
    shadowElevation: Dp = 0.dp,
    onRemove: () -> Unit,
    dragHandle: @Composable () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        shadowElevation = shadowElevation,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 12.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
        ) {
            if (user.isGhostUser) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.PersonOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(22.dp),
                    )
                }
            } else {
                ProfileImage(
                    profilePic = user.photoUrl,
                    profileSize = 40.dp,
                    userName = user.displayName,
                )
            }

            HSpacer(12.dp)

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (user.isGhostUser) {
                    Text(
                        text = stringResource(R.string.guest_label),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (user.isGhostUser) {
                IconButton(onClick = onRemove) {
                    Icon(
                        imageVector = Icons.Filled.RemoveCircleOutline,
                        contentDescription = stringResource(R.string.remove_content_description),
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }

            dragHandle()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HostRotationSheetContentPreview() {
    FlotMandTheme {
        HostRotationSheetContent(
            rotationMembers = listOf(
                User(id = "1", displayName = "Zlatan Stadler", isAnonymous = false),
                User(id = "2", displayName = "Gustav Rasslan", isAnonymous = false),
                User(id = "3", displayName = "Mikkel Rahbek", isAnonymous = false),
                User(id = "4", displayName = "Gæst", isGhostUser = true),
                User(id = "5", displayName = "Oliver Payne", isAnonymous = false),
            ),
            showAddGhostUserDialog = false,
            onAddGhostUserClick = {},
            onDismissDialog = {},
            onConfirmAddGhostUser = {},
            onRemoveUser = {},
            onSaveOrder = {},
            onResetPlacements = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RotationMemberRowRegularPreview() {
    FlotMandTheme {
        RotationMemberRow(
            user = User(id = "1", displayName = "Zlatan Stadler", isAnonymous = false),
            onRemove = {},
            dragHandle = {
                Icon(
                    imageVector = Icons.Filled.DragHandle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RotationMemberRowGhostPreview() {
    FlotMandTheme {
        RotationMemberRow(
            user = User(id = "2", displayName = "Gæst", isGhostUser = true),
            onRemove = {},
            dragHandle = {
                Icon(
                    imageVector = Icons.Filled.DragHandle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AddGhostUserDialogPreview() {
    FlotMandTheme {
        AddGhostUserDialog(
            onDismiss = {},
            onConfirm = {},
        )
    }
}

@Composable
private fun AddGhostUserDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_user_title)) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.name_label)) },
                singleLine = true,
                placeholder = { Text(stringResource(R.string.name_placeholder)) },
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name) },
                enabled = name.isNotBlank(),
            ) {
                Text(stringResource(R.string.add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}
