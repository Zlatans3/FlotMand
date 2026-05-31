@file:Suppress(
    "ktlint:standard:function-naming",
    "ktlint:standard:package-name",
    "ktlint:standard:max-line-length",
)

package dk.zlatan.flotmand.Features.frontpage.host_rotation

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dk.zlatan.flotmand.R
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dk.zlatan.flotmand.design_system.componenets.ProfileImage
import dk.zlatan.flotmand.design_system.componenets.spacers.HSpacer
import dk.zlatan.flotmand.model.User
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun HostRotationRoute(
    onDismiss: () -> Unit,
    viewModel: HostRotationViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val showDialog by viewModel.showDialog.collectAsStateWithLifecycle()

    HostRotationScreen(
        rotationMembers = uiState.rotationMembers,
        showAddGhostUserDialog = showDialog,
        onDismiss = onDismiss,
        onAddGhostUserClick = viewModel::onShowAddGhostUserDialog,
        onDismissDialog = viewModel::onDismissDialog,
        onConfirmAddGhostUser = viewModel::onAddGhostUser,
        onRemoveUser = viewModel::onRemoveUser,
        onSaveOrder = viewModel::onSaveOrder,
        onResetPlacements = viewModel::onResetPlacements,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HostRotationScreen(
    rotationMembers: List<User>,
    showAddGhostUserDialog: Boolean,
    onDismiss: () -> Unit,
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.rotation_order_title)) },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_content_description),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onAddGhostUserClick) {
                        Icon(
                            imageVector = Icons.Filled.PersonAdd,
                            contentDescription = stringResource(R.string.add_user_content_description),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        bottomBar = {
            OutlinedButton(
                onClick = onResetPlacements,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .navigationBarsPadding(),
            ) {
                Text(stringResource(R.string.reset_placements))
            }
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(
                state = lazyListState,
                modifier = Modifier.fillMaxSize(),
            ) {
                items(displayMembers, key = { it.id }) { user ->
                    ReorderableItem(reorderableState, key = user.id) { isDragging ->
                        val elevation by animateDpAsState(if (isDragging) 8.dp else 0.dp)
                        Surface(shadowElevation = elevation) {
                            RotationMemberRow(
                                user = user,
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
    onRemove: () -> Unit,
    dragHandle: @Composable () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        dragHandle()

        HSpacer(8.dp)

        if (user.isGhost) {
            Box(
                modifier = Modifier.size(40.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.PersonOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(28.dp),
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

        Text(
            text = user.displayName,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )

        if (user.isGhost) {
            Text(
                text = stringResource(R.string.guest_label),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 4.dp),
            )
        }

        IconButton(onClick = onRemove) {
            Icon(
                imageVector = Icons.Filled.RemoveCircleOutline,
                contentDescription = stringResource(R.string.remove_content_description),
                tint = MaterialTheme.colorScheme.error,
            )
        }
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
