package dk.zlatan.flotmand.design_system.componenets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import dk.zlatan.flotmand.design_system.theme.FlotMandTheme
import dk.zlatan.flotmand.model.User

@Composable
fun OverlappingAvatars(
    participants: List<User>,
    avatarSize: Dp = 32.dp,
    containerColor: Color = MaterialTheme.colorScheme.background
) {
    val overlap = 8.dp
    val visible = participants.take(3)
    val showOverflow = participants.size > 3

    // Calculate spacing between avatars (avatarSize - overlap)
    val spacing = avatarSize - overlap

    // Calculate total width: first avatar + spacing * remaining avatars (+ overflow badge if needed)
    val totalWidth = if (showOverflow) {
        avatarSize + (spacing * visible.size) + spacing
    } else {
        avatarSize + (spacing * (visible.size - 1))
    }

    Box(
        modifier = Modifier
            .width(totalWidth)
            .height(avatarSize)
    ) {
        visible.forEachIndexed { index, user ->
            Box(
                modifier = Modifier
                    .size(avatarSize)
                    .offset(x = spacing * index)
                    .border(
                        width = 2.dp,
                        color = containerColor,
                        shape = CircleShape
                    )
                    .zIndex((visible.size + index).toFloat())
            ) {
                ProfileImage(
                    modifier = Modifier
                        .size(avatarSize),
                    profilePic = user.photoUrl,
                    userName = user.displayName
                )
            }
        }
        if (showOverflow) {
            Box(
                modifier = Modifier
                    .offset(x = spacing * visible.size)
                    .size(avatarSize)
                    .clip(CircleShape)
                    .border(
                        width = 2.dp,
                        color = containerColor,
                        shape = CircleShape
                    )
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .zIndex((visible.size * 2).toFloat()),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+${participants.size - visible.size}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Preview
@Composable
private fun OverlappingAvatarsPreview() {
    FlotMandTheme {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            OverlappingAvatars(
                participants = User.mockUserWithCounter(5),
                containerColor = MaterialTheme.colorScheme.background
            )
        }
    }
}
