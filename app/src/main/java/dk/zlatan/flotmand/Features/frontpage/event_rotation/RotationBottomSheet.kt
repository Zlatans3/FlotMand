@file:Suppress(
    "ktlint:standard:function-naming",
    "ktlint:standard:max-line-length",
    "ktlint:standard:multiline-expression-wrapping",
    "ktlint:standard:package-name",
)

package dk.zlatan.flotmand.Features.frontpage.event_rotation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dk.zlatan.flotmand.R
import dk.zlatan.flotmand.design_system.componenets.ProfileImage
import dk.zlatan.flotmand.design_system.componenets.spacers.VSpacer
import dk.zlatan.flotmand.model.User

sealed class RotationBottomSheetState {
    object Hidden : RotationBottomSheetState()
    data class HostOptions(val monthId: String, val hostId: String, val hostName: String) : RotationBottomSheetState()
    data class UserPicker(val monthId: String) : RotationBottomSheetState()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RotationBottomSheet(
    state: RotationBottomSheetState,
    members: List<User>,
    onDismiss: () -> Unit,
    onGiveUpSpot: (monthId: String, hostId: String) -> Unit,
    onShowUserPicker: (monthId: String) -> Unit,
    onRemoveFromRotation: (hostId: String) -> Unit,
    onAssignUser: (monthId: String, userId: String) -> Unit,
    onSeeProfile: ((userId: String) -> Unit)? = null,
) {
    if (state is RotationBottomSheetState.Hidden) return

    ModalBottomSheet(onDismissRequest = onDismiss) {
        when (state) {
            is RotationBottomSheetState.HostOptions -> {
                // Ghost users have no profile to show.
                val hostIsGhost = members.firstOrNull { it.id == state.hostId }?.isGhostUser != false
                HostOptionsContent(
                    hostName = state.hostName,
                    onGiveUpSpot = { onGiveUpSpot(state.monthId, state.hostId) },
                    onReplaceUser = { onShowUserPicker(state.monthId) },
                    onRemoveFromRotation = { onRemoveFromRotation(state.hostId) },
                    onSeeProfile = if (onSeeProfile != null && !hostIsGhost) {
                        { onSeeProfile(state.hostId) }
                    } else null,
                )
            }

            is RotationBottomSheetState.UserPicker -> UserPickerContent(
                members = members,
                onUserSelected = { userId -> onAssignUser(state.monthId, userId) },
            )

            else -> {}
        }
    }
}

@Composable
private fun HostOptionsContent(
    hostName: String,
    onGiveUpSpot: () -> Unit,
    onReplaceUser: () -> Unit,
    onRemoveFromRotation: () -> Unit,
    onSeeProfile: (() -> Unit)? = null,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = hostName,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        VSpacer(8.dp)
        if (onSeeProfile != null) {
            ActionRow(
                icon = Icons.Filled.Person,
                label = stringResource(R.string.see_profile),
                onClick = onSeeProfile,
            )
        }
        ActionRow(
            icon = Icons.AutoMirrored.Filled.ExitToApp,
            label = stringResource(R.string.rotation_pass_spot),
            onClick = onGiveUpSpot,
        )
        ActionRow(
            icon = Icons.Filled.Edit,
            label = stringResource(R.string.rotation_change_host),
            onClick = onReplaceUser,
        )
        ActionRow(
            icon = Icons.Filled.Delete,
            label = stringResource(R.string.rotation_remove_from_order),
            color = MaterialTheme.colorScheme.error,
            iconTint = MaterialTheme.colorScheme.error,
            onClick = onRemoveFromRotation,
        )
        VSpacer(32.dp)
    }
}

@Composable
private fun UserPickerContent(
    members: List<User>,
    onUserSelected: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.rotation_choose_person),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        VSpacer(8.dp)
        LazyColumn {
            items(members, key = { it.id }) { user ->
                UserRow(user = user, onClick = { onUserSelected(user.id) })
            }
        }
        VSpacer(32.dp)
    }
}

@Composable
private fun ActionRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    color: Color = MaterialTheme.colorScheme.onSurface,
    iconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(22.dp),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = color,
            modifier = Modifier.padding(start = 16.dp),
        )
    }
}

@Composable
private fun UserRow(
    user: User,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 12.dp),
    ) {
        ProfileImage(
            profilePic = user.photoUrl,
            profileSize = 40.dp,
            userName = user.displayName,
        )
        Text(
            text = user.displayName,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 12.dp),
        )
    }
}
