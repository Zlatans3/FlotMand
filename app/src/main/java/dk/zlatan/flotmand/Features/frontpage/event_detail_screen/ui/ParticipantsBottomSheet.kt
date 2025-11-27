package dk.zlatan.flotmand.Features.frontpage.event_detail_screen.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
    onDismiss: () -> Unit,
    ) {

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
    ) {
        ParticipantsContent(
            modifier = Modifier,
            participants = participants
        )
    }
}

@Composable
internal fun ParticipantsContent(
    modifier: Modifier = Modifier,
    participants: List<User> = emptyList(),
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            modifier = Modifier.padding(horizontal = 20.dp),
            text = "Deltagere",
            style = MaterialTheme.typography.headlineMedium,
        )

        VSpacer(12.dp)
        participants.forEach { participant ->
            ProfileParticipant(
                modifier = Modifier,
                profilePic = participant.photoUrl,
                userName = participant.displayName
            )
            HSpacer(16.dp)
        }
    }
}

@Composable
fun ProfileParticipant(
    modifier: Modifier = Modifier,
    profilePic: String? = null,
    userName: String,
    ) {
    Row(
        modifier = modifier
            .clickable(
                onClick = { /* No action for now */ }
            )
            .padding(14.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
        ) {
        ProfileImage(
            modifier = Modifier,
            profilePic = profilePic,
            userName = userName
        )
        HSpacer(12.dp)
        Text(
            text = userName,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ParticipantsContentPreview() {
    val mockUserList = listOf(
        User(displayName = "Alice Johnson"),
        User(displayName = "Bob Smith"),
        User(displayName = "Charlie Brown"),
    )
    ParticipantsContent(
        modifier = Modifier,
        participants = mockUserList
        )
}