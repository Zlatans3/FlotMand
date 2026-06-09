package dk.zlatan.flotmand.Features.frontpage.notifications

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import dk.zlatan.flotmand.design_system.componenets.ProfileImage
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import dk.zlatan.flotmand.R
import dk.zlatan.flotmand.design_system.componenets.spacers.VSpacer
import dk.zlatan.flotmand.design_system.componenets.topappbar.FmTopAppBar
import dk.zlatan.flotmand.design_system.icon.FmIcons
import dk.zlatan.flotmand.design_system.theme.FlotMandTheme
import dk.zlatan.flotmand.model.AppNotification
import dk.zlatan.flotmand.model.NotificationType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onEventClick: (eventId: String) -> Unit,
    onPollClick: (votingId: String) -> Unit,
    viewModel: NotificationsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var dismissingIds by remember { mutableStateOf(setOf<String>()) }

    val onBack: () -> Unit = {
        viewModel.markAllAsRead()
        onDismiss()
    }

    BackHandler(onBack = onBack)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            FmTopAppBar(
                textContent = {
                    Text(
                        text = stringResource(R.string.notifications_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                },
                leadingIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = FmIcons.ArrowBack,
                            contentDescription = stringResource(R.string.close),
                            tint = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                },
                trailingContent = {
                    if (uiState.notifications.isNotEmpty()) {
                        TextButton(onClick = { viewModel.dismissAll() }) {
                            Text(
                                text = stringResource(R.string.notifications_clear_all),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
            )
        },
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier =
                Modifier
                    .padding(top = paddingValues.calculateTopPadding())
                    .fillMaxSize(),
        ) {
            if (uiState.notifications.isEmpty() && !uiState.isLoading) {
                NotificationsEmptyState(modifier = Modifier.fillMaxSize())
            } else {
                LazyColumn(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background),
                ) {
                    items(uiState.notifications, key = { it.id }) { notification ->
                        AnimatedVisibility(
                            visible = notification.id !in dismissingIds,
                            enter = EnterTransition.None,
                            exit = slideOutHorizontally(
                                targetOffsetX = { fullWidth -> fullWidth },
                                animationSpec = tween(durationMillis = 280),
                            ) + fadeOut(animationSpec = tween(durationMillis = 220)),
                        ) {
                            NotificationItem(
                                notification = notification,
                                onClick = {
                                    viewModel.markAsRead(notification.id)
                                    when (notification.notificationType) {
                                        NotificationType.EVENT -> onEventClick(notification.referenceId)
                                        NotificationType.POLL -> onPollClick(notification.referenceId)
                                        NotificationType.UNKNOWN -> Unit
                                    }
                                },
                                onDismiss = {
                                    dismissingIds = dismissingIds + notification.id
                                    scope.launch {
                                        delay(300)
                                        viewModel.dismiss(notification.id)
                                        dismissingIds = dismissingIds - notification.id
                                    }
                                },
                            )
                        }
                    }
                    item { VSpacer(24.dp) }
                }
            }
        }
    }
}

@Composable
private fun NotificationItem(
    notification: AppNotification,
    onClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val type = notification.notificationType

    val cardColor by animateColorAsState(
        targetValue =
            if (notification.isRead) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            },
        animationSpec = tween(durationMillis = 300),
        label = "notificationBg",
    )

    val badgeBgColor =
        when (type) {
            NotificationType.EVENT -> MaterialTheme.colorScheme.tertiaryContainer
            NotificationType.POLL -> MaterialTheme.colorScheme.secondaryContainer
            NotificationType.UNKNOWN -> MaterialTheme.colorScheme.surfaceVariant
        }
    val badgeTint =
        when (type) {
            NotificationType.EVENT -> MaterialTheme.colorScheme.onTertiaryContainer
            NotificationType.POLL -> MaterialTheme.colorScheme.onSecondaryContainer
            NotificationType.UNKNOWN -> MaterialTheme.colorScheme.onSurfaceVariant
        }
    val typeIcon =
        when (type) {
            NotificationType.EVENT -> FmIcons.NotificationEvent
            NotificationType.POLL -> FmIcons.NotificationPoll
            NotificationType.UNKNOWN -> FmIcons.Bell
        }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.size(44.dp)) {
                ProfileImage(
                    profilePic = notification.senderPhotoUrl.ifBlank { null },
                    profileSize = 44.dp,
                    userName = notification.senderDisplayName.ifBlank { "?" },
                )
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(badgeBgColor)
                        .align(Alignment.BottomEnd),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = typeIcon,
                        contentDescription = null,
                        tint = badgeTint,
                        modifier = Modifier.size(11.dp),
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (!notification.body.isNullOrBlank()) {
                    Text(
                        text = notification.body,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                    )
                }
                Text(
                    text = formatRelativeTime(notification.createdAtMillis),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 2.dp),
                )
            }

            if (!notification.isRead) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 6.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                )
            }

            IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = FmIcons.Close,
                    contentDescription = stringResource(R.string.notifications_dismiss),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

private fun formatRelativeTime(createdAtMillis: Long): String {
    if (createdAtMillis == 0L) return ""
    val diff = System.currentTimeMillis() - createdAtMillis
    val minutes = diff / 60_000
    val hours = diff / 3_600_000
    val days = diff / 86_400_000
    return when {
        minutes < 1 -> "Lige nu"
        minutes < 60 -> "For $minutes min siden"
        hours < 24 -> "For $hours time${if (hours > 1L) "r" else ""} siden"
        days == 1L -> "I går"
        days < 7 -> "For $days dage siden"
        else -> SimpleDateFormat("d. MMM", Locale.forLanguageTag("da-DK")).format(Date(createdAtMillis))
    }
}

@Composable
private fun NotificationsEmptyState(modifier: Modifier = Modifier) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.bell_sleeping),
    )
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        LottieAnimation(
            composition = composition,
            iterations = LottieConstants.IterateForever,
            modifier = Modifier
                .size(240.dp),
        )
        VSpacer(8.dp)
        Text(
            text = stringResource(R.string.notifications_empty_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        VSpacer(4.dp)
        Text(
            text = stringResource(R.string.notifications_empty_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NotificationItemUnreadPreview() {
    FlotMandTheme(dynamicColor = false) {
        Column {
            NotificationItem(
                notification =
                    AppNotification(
                        id = "1",
                        type = NotificationType.EVENT.value,
                        referenceId = "e1",
                        title = "🍽️ Nyt event oprettet",
                        body = "Middag hos Gustav",
                        isRead = false,
                        createdAtMillis = System.currentTimeMillis(),
                    ),
                onClick = {},
                onDismiss = {},
            )
            NotificationItem(
                notification =
                    AppNotification(
                        id = "2",
                        type = NotificationType.POLL.value,
                        referenceId = "p1",
                        title = "🗳️ Ny afstemning oprettet",
                        body = "Hvornår passer det?",
                        isRead = true,
                        createdAtMillis = System.currentTimeMillis(),
                    ),
                onClick = {},
                onDismiss = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NotificationsEmptyPreview() {
    FlotMandTheme(dynamicColor = false) {
        NotificationsEmptyState(modifier = Modifier.fillMaxSize())
    }
}
