package dk.zlatan.flotmand.Features.frontpage.event_detail_screen.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
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
    publisherId: String? = null,
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
            publisherId = publisherId,
        )
    }
}

@Composable
internal fun ParticipantsContent(
    modifier: Modifier = Modifier,
    participants: List<User> = emptyList(),
    publisherId: String? = null,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            modifier = Modifier.padding(horizontal = 20.dp),
            text = stringResource(R.string.participants_label),
            style = MaterialTheme.typography.headlineMedium,
        )

        VSpacer(12.dp)
        participants.forEach { participant ->
            val isHost = publisherId != null && participant.id == publisherId
            val displayName =
                if (isHost) stringResource(R.string.participant_host, participant.displayName) else participant.displayName

            ProfileParticipant(
                modifier = Modifier,
                profilePic = participant.photoUrl,
                userName = displayName,
            )
            HSpacer(16.dp)
        }
        VSpacer(40.dp)
    }
}

@Composable
fun ProfileParticipant(
    modifier: Modifier = Modifier,
    profilePic: String? = null,
    userName: String,
) {
    Row(
        modifier =
            modifier
                .clickable(
                    onClick = { /* No action for now */ },
                ).padding(14.dp)
                .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ProfileImage(
            modifier = Modifier,
            profilePic = profilePic,
            userName = userName,
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
    val mockUserList =
        listOf(
            User(id = "u1", displayName = "Gustav Rasslan"),
            User(id = "u2", displayName = "Oliver Payne"),
            User(id = "u3", displayName = "Mikkel Rahbek"),
        )
    ParticipantsContent(
        modifier = Modifier,
        participants = mockUserList,
        publisherId = mockUserList[1].id,
    )
}
