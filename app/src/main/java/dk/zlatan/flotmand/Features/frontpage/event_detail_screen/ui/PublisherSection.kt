package dk.zlatan.flotmand.Features.frontpage.event_detail_screen.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dk.zlatan.flotmand.design_system.componenets.ProfileImage
import dk.zlatan.flotmand.model.User

@Composable
internal fun PublisherSection(
    modifier: Modifier = Modifier,
    publisher: User?,
    isPublisher: Boolean,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ProfileImage(
            modifier = Modifier.border(
                width = 2.dp,
                color = if (isPublisher) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.inversePrimary,
                shape = RoundedCornerShape(24.dp)
            ),
            profilePic = publisher?.photoUrl,
            profileSize = 48.dp,
            userName = publisher?.displayName.orEmpty(),
        )
        Column(
            modifier = Modifier.padding(start = 12.dp)
        ) {
            Text(
                text = "Arrangør",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = if (isPublisher) "Dig" else publisher?.getFirstName().orEmpty(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (!isPublisher && !publisher?.email.isNullOrEmpty()) {
                Text(
                    text = publisher.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PublisherSectionPreview() {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        PublisherSection(
            modifier = Modifier,
            publisher = User.mockUserWithCounter(1).first(),
            isPublisher = false,
        )
        PublisherSection(
            modifier = Modifier,
            publisher = User.mockUserWithCounter(2).first(),
            isPublisher = true,
        )
    }
}