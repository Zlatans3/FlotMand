package dk.zlatan.flotmand.Features.frontpage.event_detail_screen.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import dk.zlatan.flotmand.R
import dk.zlatan.flotmand.design_system.componenets.ProfileImage
import dk.zlatan.flotmand.design_system.componenets.spacers.HSpacer
import dk.zlatan.flotmand.design_system.componenets.spacers.VSpacer
import dk.zlatan.flotmand.model.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParticipantsBottomSheet(
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
    participants: List<User>,
    declinedUsers: List<User> = emptyList(),
    publisherId: String? = null,
    availableGhosts: List<User> = emptyList(),
    showAddGhostDialog: Boolean = false,
    onShowAddGhostDialog: () -> Unit = {},
    onDismissAddGhostDialog: () -> Unit = {},
    onAddGhostToList: (userId: String, attending: Boolean) -> Unit = { _, _ -> },
    onRemoveGhost: ((userId: String) -> Unit)? = null,
    onUserClick: ((userId: String) -> Unit)? = null,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
    ) {
        ParticipantsContent(
            modifier = Modifier,
            participants = participants,
            declinedUsers = declinedUsers,
            publisherId = publisherId,
            availableGhosts = availableGhosts,
            onShowAddGhostDialog = onShowAddGhostDialog,
            onRemoveGhost = onRemoveGhost,
            onUserClick = onUserClick,
        )
    }

    if (showAddGhostDialog) {
        AddGhostToEventDialog(
            ghosts = availableGhosts,
            onDismiss = onDismissAddGhostDialog,
            onAddToList = onAddGhostToList,
        )
    }
}

@Composable
internal fun ParticipantsContent(
    modifier: Modifier = Modifier,
    participants: List<User> = emptyList(),
    declinedUsers: List<User> = emptyList(),
    publisherId: String? = null,
    availableGhosts: List<User> = emptyList(),
    onShowAddGhostDialog: () -> Unit = {},
    onRemoveGhost: ((userId: String) -> Unit)? = null,
    onUserClick: ((userId: String) -> Unit)? = null,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.participants_label),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.weight(1f),
            )
            if (availableGhosts.isNotEmpty()) {
                IconButton(onClick = onShowAddGhostDialog) {
                    Icon(
                        imageVector = Icons.Filled.PersonAdd,
                        contentDescription = stringResource(R.string.add_user_content_description),
                    )
                }
            }
        }

        VSpacer(12.dp)

        if (participants.isNotEmpty()) {
            Text(
                modifier = Modifier.padding(horizontal = 20.dp),
                text = stringResource(R.string.participants_attending_label),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            participants.forEach { participant ->
                val isHost = publisherId != null && participant.id == publisherId
                val displayName =
                    if (isHost) stringResource(R.string.participant_host, participant.displayName) else participant.displayName
                ProfileParticipant(
                    profilePic = participant.photoUrl,
                    userName = displayName,
                    isGhostUser = participant.isGhostUser,
                    onRemove = if (participant.isGhostUser && onRemoveGhost != null) {
                        { onRemoveGhost(participant.id) }
                    } else null,
                    onSeeProfile = if (!participant.isGhostUser && onUserClick != null) {
                        { onUserClick(participant.id) }
                    } else null,
                )
            }
        }

        if (declinedUsers.isNotEmpty()) {
            if (participants.isNotEmpty()) {
                VSpacer(8.dp)
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                VSpacer(8.dp)
            }
            Text(
                modifier = Modifier.padding(horizontal = 20.dp),
                text = stringResource(R.string.participants_declined_label),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            declinedUsers.forEach { user ->
                ProfileParticipant(
                    profilePic = user.photoUrl,
                    userName = user.displayName,
                    muted = true,
                    isGhostUser = user.isGhostUser,
                    onRemove = if (user.isGhostUser && onRemoveGhost != null) {
                        { onRemoveGhost(user.id) }
                    } else null,
                    onSeeProfile = if (!user.isGhostUser && onUserClick != null) {
                        { onUserClick(user.id) }
                    } else null,
                )
            }
        }

        VSpacer(40.dp)
    }
}

@Composable
fun ProfileParticipant(
    modifier: Modifier = Modifier,
    profilePic: String? = null,
    userName: String,
    muted: Boolean = false,
    isGhostUser: Boolean = false,
    onRemove: (() -> Unit)? = null,
    onSeeProfile: (() -> Unit)? = null,
) {
    var showMenu by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .clickable(
                    enabled = onSeeProfile != null,
                    onClick = { showMenu = true },
                )
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (isGhostUser) {
                Icon(
                    imageVector = Icons.Filled.PersonOff,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp).padding(4.dp),
                    tint = if (muted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                )
            } else {
                ProfileImage(
                    modifier = Modifier,
                    profilePic = profilePic,
                    userName = userName,
                )
            }
            HSpacer(12.dp)
            Text(
                text = userName,
                style = MaterialTheme.typography.bodyLarge,
                color = if (muted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            if (isGhostUser) {
                HSpacer(4.dp)
                Text(
                    text = stringResource(R.string.guest_label),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (onRemove != null) {
                IconButton(onClick = onRemove) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            offset = DpOffset(x = 56.dp, y = 0.dp),
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.see_profile)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                    )
                },
                onClick = {
                    showMenu = false
                    onSeeProfile?.invoke()
                },
            )
        }
    }
}

@Composable
private fun AddGhostToEventDialog(
    ghosts: List<User>,
    onDismiss: () -> Unit,
    onAddToList: (userId: String, attending: Boolean) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Filled.PersonAdd,
                contentDescription = null,
            )
        },
        title = { Text(stringResource(R.string.add_user_title)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .heightIn(max = 320.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                ghosts.forEach { ghost ->
                    AddGhostRow(ghost = ghost, onAddToList = onAddToList)
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

@Composable
private fun AddGhostRow(
    ghost: User,
    onAddToList: (userId: String, attending: Boolean) -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.PersonOff,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
            HSpacer(12.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = ghost.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = stringResource(R.string.guest_label),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            HSpacer(8.dp)
            FilledTonalIconButton(
                onClick = { onAddToList(ghost.id, true) },
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = stringResource(R.string.participants_attending_label),
                )
            }
            FilledTonalIconButton(
                onClick = { onAddToList(ghost.id, false) },
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                ),
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = stringResource(R.string.participants_declined_label),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AddGhostToEventDialogPreview() {
    AddGhostToEventDialog(
        ghosts = listOf(
            User(id = "g1", displayName = "Peter", isGhostUser = true),
            User(id = "g2", displayName = "Anders Andersen", isGhostUser = true),
        ),
        onDismiss = {},
        onAddToList = { _, _ -> },
    )
}

@Preview(showBackground = true)
@Composable
private fun ParticipantsContentPreview() {
    val mockUserList = listOf(
        User(id = "u1", displayName = "Gustav Rasslan"),
        User(id = "u2", displayName = "Oliver Payne"),
        User(id = "u3", displayName = "Mikkel Rahbek"),
    )
    val mockDeclined = listOf(
        User(id = "u4", displayName = "David Sandell"),
        User(id = "u5", displayName = "Gæst", isGhostUser = true),
    )
    val mockGhosts = listOf(
        User(id = "g1", displayName = "Peter", isGhostUser = true),
    )
    ParticipantsContent(
        modifier = Modifier,
        participants = mockUserList,
        declinedUsers = mockDeclined,
        publisherId = mockUserList[1].id,
        availableGhosts = mockGhosts,
        onShowAddGhostDialog = {},
    )
}
