package dk.zlatan.flotmand.Features.frontpage.event_detail_screen.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
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
) {
    Row(
        modifier = modifier
            .clickable(onClick = { })
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
}

@Composable
private fun AddGhostToEventDialog(
    ghosts: List<User>,
    onDismiss: () -> Unit,
    onAddToList: (userId: String, attending: Boolean) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_user_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                ghosts.forEach { ghost ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PersonOff,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            HSpacer(8.dp)
                            Text(
                                text = ghost.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f),
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                        ) {
                            TextButton(onClick = { onAddToList(ghost.id, true) }) {
                                Text(stringResource(R.string.participants_attending_label))
                            }
                            TextButton(onClick = { onAddToList(ghost.id, false) }) {
                                Text(stringResource(R.string.participants_declined_label))
                            }
                        }
                    }
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
