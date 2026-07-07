package dk.zlatan.flotmand.Features.frontpage.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dk.zlatan.flotmand.R
import dk.zlatan.flotmand.design_system.componenets.ProfileImage
import dk.zlatan.flotmand.design_system.componenets.spacers.HSpacer
import dk.zlatan.flotmand.design_system.componenets.spacers.VSpacer
import dk.zlatan.flotmand.design_system.icon.FmIcons
import dk.zlatan.flotmand.model.User
import kotlin.math.pow

@Composable
internal fun FrontPageNewHeader(
    user: User,
    modifier: Modifier = Modifier,
    scrollProgress: Float = 0f,
) {
    // Sharpening starts on scroll and ends when the top bar takes over (scrollProgress ~1)
    // Use a gentle curve so the sharpness transition takes a long time
    val normalized = scrollProgress.coerceIn(0f, 1f)
    val animatedNormalized by animateFloatAsState(targetValue = normalized)
    // Use a gentle curve (e.g. pow(1.5)) so the corners sharpen gradually
    val sharpness = animatedNormalized.pow(1.5f)
    val cornerRadius = 24.dp * (1f - sharpness)
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape =
                        RoundedCornerShape(
                            topStart = 0.dp,
                            topEnd = 0.dp,
                            bottomStart = cornerRadius,
                            bottomEnd = cornerRadius,
                        ),
                ),
    ) {
        HeaderContent(
            modifier = Modifier,
            name = user.getFirstName(),
            scrollProgress = scrollProgress,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun newFmTopAppBar(
    modifier: Modifier = Modifier,
    user: User,
    unreadNotificationCount: Int,
    onNotificationsClick: () -> Unit = {},
    onUserClicked: (() -> Unit)?,
    onUserLongClicked: (() -> Unit)? = null,
) {
    val insert = TopAppBarDefaults.windowInsets
    Column(modifier = modifier) {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            },
            navigationIcon = {
                Row {
                    HSpacer(10.dp)
                    Image(
                        painter = painterResource(id = R.drawable.flotmandapp),
                        contentDescription = stringResource(R.string.flotmand_logo_content_description),
                        modifier =
                            Modifier
                                .size(40.dp)
                                .padding(start = 8.dp),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                    )
                }
            },
            actions = {
                val haptic = LocalHapticFeedback.current

                // Bell icon with unread badge
                IconButton(onClick = onNotificationsClick) {
                    BadgedBox(
                        badge = {
                            if (unreadNotificationCount > 0) {
                                Badge {
                                    Text(
                                        text = if (unreadNotificationCount > 9) "9+" else "$unreadNotificationCount",
                                        style = MaterialTheme.typography.labelSmall,
                                    )
                                }
                            }
                        },
                    ) {
                        Icon(
                            imageVector = if (unreadNotificationCount > 0) FmIcons.Bell else FmIcons.BellOutline,
                            contentDescription = stringResource(R.string.notifications_title),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(top = 3.dp, end = 3.dp),
                        )
                    }
                }

                HSpacer(4.dp)

                // Profile image with border
                ProfileImage(
                    modifier =
                        Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    profilePic = user.photoUrl,
                    userName = user.getFirstName(),
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onUserClicked?.invoke()
                    },
                    onLongClick = onUserLongClicked,
                )
                HSpacer(20.dp)
            },
            windowInsets = insert,
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                ),
            scrollBehavior = null,
        )
    }
}

@Composable
fun HeaderContent(
    modifier: Modifier = Modifier,
    name: String,
    scrollProgress: Float = 0f,
) {
    val alpha = 1f - scrollProgress
    val translationY = -80f * scrollProgress

    Column(
        modifier =
            modifier
                .graphicsLayer {
                    this.alpha = alpha
                    this.translationY = translationY
                },
    ) {
        VSpacer(40.dp)
        Text(
            text = stringResource(R.string.greeting_with_name, name),
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 24.dp),
        )

        VSpacer(8.dp)

        Text(
            text = stringResource(R.string.welcome_back),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
            modifier = Modifier.padding(horizontal = 24.dp),
        )

        VSpacer(40.dp)
    }
}

@Preview
@Composable
private fun NewFmTopAppBarPreview() {
    newFmTopAppBar(
        modifier = Modifier.graphicsLayer {},
        user = User.mockUserWithCounter(1).first(),
        onUserClicked = { },
        unreadNotificationCount = 0,
    )
}

@Preview
@Composable
private fun FrontPageTopBarPreview() {
    FrontPageNewHeader(
        modifier = Modifier.graphicsLayer {},
        user = User.mockUserWithCounter(1).first(),
    )
}
