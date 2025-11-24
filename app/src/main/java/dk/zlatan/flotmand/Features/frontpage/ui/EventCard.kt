package dk.zlatan.flotmand.Features.frontpage.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dk.zlatan.flotmand.design_system.componenets.ProfileImage
import dk.zlatan.flotmand.design_system.componenets.spacers.HSpacer
import dk.zlatan.flotmand.design_system.componenets.spacers.VSpacer
import kotlin.String

@Composable
internal fun EventCard(
    modifier: Modifier = Modifier,
    @DrawableRes userProfilePic: Int? = null,
    userName: String,
    eventName: String,
    eventDate: String,
    eventTime: String,

    ) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.inverseOnSurface
        ), // find nogle farver der passer bedre
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        VSpacer(height = 20.dp)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
        ) {
            ProfileImage(
                modifier = Modifier,
                profilePic = userProfilePic,
                userNameInitials = userName
                    .split(" ")
                    .mapNotNull { it.firstOrNull()?.toString() }
                    .take(2)
                    .joinToString("")
            )

            HSpacer(12.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = userName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    modifier = Modifier,
                )

                VSpacer(12.dp)

                Text(
                    text = eventName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                VSpacer(4.dp)

                Text(
                    text = "$eventDate - $eventTime",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        VSpacer(height = 20.dp)
    }
}

@Preview()
@Composable
private fun EventCardPreview() {
    EventCard(
        modifier = Modifier,
        userProfilePic = null,
        userName = "Zlatan Stadler",
        eventDate = "06-15",
        eventTime = "18:00",
        eventName = "Middag hos Zlatan"
    )
}
