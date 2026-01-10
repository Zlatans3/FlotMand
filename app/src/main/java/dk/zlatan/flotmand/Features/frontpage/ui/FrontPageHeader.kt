package dk.zlatan.flotmand.Features.frontpage.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dk.zlatan.flotmand.R
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import dk.zlatan.flotmand.design_system.componenets.spacers.VSpacer
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import dk.zlatan.flotmand.design_system.componenets.spacers.HSpacer
import dk.zlatan.flotmand.model.User
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import dk.zlatan.flotmand.design_system.componenets.ProfileImage

@Composable
internal fun FrontPageNewHeader(
    user: User,
    modifier: Modifier = Modifier,
    scrollProgress: Float = 0f,
    ) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(
                    topStart = 0.dp,
                    topEnd = 0.dp,
                    bottomStart = 24.dp,
                    bottomEnd = 24.dp
                )
            ),

    ) {
        HeaderContent(
            modifier = Modifier,
            name = user.getFirstName(),
            scrollProgress = scrollProgress
        )
    }
}

@Composable
fun FmAnimatableTopBar(
    modifier: Modifier = Modifier,
    user: User,
    scrollProgress: Float = 0f,
    onClick: () -> Unit = { }
    ) {
    val cornerRadius = (24.dp * scrollProgress)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(
                    topStart = 0.dp,
                    topEnd = 0.dp,
                    bottomStart = cornerRadius,
                    bottomEnd = cornerRadius
                )
            )
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.flotmandapp),
            contentDescription = "Flotmand Logo",
            modifier = Modifier
                .size(40.dp),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
        )

        HSpacer(12.dp)

        Text(
            text = "Flotmand",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.weight(1f))

        // Notification Bell Icon with circular background
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    shape = CircleShape
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Notifications,
                contentDescription = "Notifications",
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.surfaceVariant,
            )
        }

        HSpacer(12.dp)

        // User Profile Icon
        ProfileImage(
            modifier = Modifier,
            profilePic = user.photoUrl,
            userName = user.getFirstName(),
            onClick = onClick
        )
    }
}

@Composable
fun HeaderContent(
    modifier: Modifier = Modifier,
    name: String,
    scrollProgress: Float = 0f
    ) {
    val alpha = 1f - scrollProgress
    val translationY = -80f * scrollProgress

    Column(
        modifier = modifier
            .graphicsLayer {
                this.alpha = alpha
                this.translationY = translationY
            }
    ) {
        VSpacer(40.dp)
        Text(
            text = "Hej, ${name}! 👋",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 24.dp),
        )

        VSpacer(8.dp)

        Text(
            text = "Velkommen tilbage til Flotte Mand!",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
            modifier = Modifier.padding(horizontal = 24.dp),
        )

        VSpacer(40.dp)
    }
}

@Preview
@Composable
private fun FmAnimatableTopBarPreview() {
    FmAnimatableTopBar(
        modifier = Modifier.graphicsLayer {},
        user = User.mockUserWithCounter(1).first()
    )
}

@Preview
@Composable
private fun FrontPageTopBarPreview() {
    FrontPageNewHeader(
        modifier = Modifier.graphicsLayer {},
        user = User.mockUserWithCounter(1).first()
    )
}
