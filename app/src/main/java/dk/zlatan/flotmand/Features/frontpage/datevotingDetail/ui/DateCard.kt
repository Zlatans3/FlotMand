package dk.zlatan.flotmand.Features.frontpage.datevotingDetail.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dk.zlatan.flotmand.R
import dk.zlatan.flotmand.design_system.componenets.OverlappingAvatars
import dk.zlatan.flotmand.design_system.componenets.spacers.HSpacer
import dk.zlatan.flotmand.design_system.componenets.spacers.VSpacer
import dk.zlatan.flotmand.design_system.theme.FlotMandTheme
import dk.zlatan.flotmand.model.User

@Composable
internal fun DateCard(
    date: String,
    subtitle: String,
    voters: List<User>,
    votePercentage: Int,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onDeleteVoteOption: () -> Unit,
) {
    val accentColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant

    // Animate the progress value
    val animatedProgress by animateFloatAsState(
        targetValue = votePercentage / 100f,
        animationSpec = tween(durationMillis = 600),
        label = "progressAnimation",
    )

    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = surfaceColor,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            // Top row with date and checkbox
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Left side: Calendar icon and date info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f),
                ) {
                    // Calendar icon
                    Box(
                        modifier =
                            Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(accentColor),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CalendarToday,
                            contentDescription = stringResource(R.string.date_icon_content_description),
                            tint = Color.White,
                            modifier = Modifier.size(20.dp),
                        )
                    }

                    HSpacer(12.dp)

                    Column {
                        Text(
                            text = date,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                // Optimized right side
                VoteOptionAction(
                    votePercentage = votePercentage,
                    isSelected = isSelected,
                    accentColor = accentColor,
                    onDeleteVoteOption = onDeleteVoteOption,
                )
            }

            VSpacer(12.dp)

            // Participants row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                // Overlapping avatars
                OverlappingAvatars(
                    participants = voters,
                    containerColor = surfaceColor,
                    avatarSize = 32.dp,
                )

                // Vote percentage (right side)
                Text(
                    text = stringResource(R.string.vote_percentage, votePercentage),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }

            VSpacer(8.dp)

            // Progress bar
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp)),
                color = accentColor,
                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
            )
        }
    }
}

@Composable
private fun VoteOptionAction(
    votePercentage: Int,
    isSelected: Boolean,
    accentColor: Color,
    onDeleteVoteOption: () -> Unit,
) {
    if (votePercentage == 0) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.Transparent)
                .clickable(onClick = onDeleteVoteOption),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = stringResource(R.string.delete_vote_option_content_description),
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(18.dp),
            )
        }
    } else {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(
                    if (isSelected) accentColor else Color.Transparent,
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}


@Preview
@Composable
private fun DateCardPreview() {
    FlotMandTheme {
        Box(
            modifier = Modifier.background(MaterialTheme.colorScheme.background).padding(16.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DateCard(
                    date = "Friday, Oct 20th",
                    subtitle = "LEADING CHOICE",
                    voters = listOf(
                        User(id = "1", displayName = "lasse"),
                        User(id = "2", displayName = "Zlatan"),
                        User(id = "3", displayName = "David"),
                    ),
                    votePercentage = 85,
                    isSelected = true,
                    onDeleteVoteOption = {},
                )
                DateCard(
                    date = "Saturday, Oct 21st",
                    subtitle = "2 friends voted",
                    voters = listOf(
                        User(id = "1", displayName = "Mikkel"),
                        User(id = "2", displayName = "Gustav"),
                    ),
                    votePercentage = 40,
                    isSelected = false,
                    onDeleteVoteOption = {},
                )
                DateCard(
                    date = "Thursday, Oct 19th",
                    subtitle = "1 friend voted",
                    voters = listOf(
                        User(id = "1", displayName = "David"),
                    ),
                    votePercentage = 15,
                    isSelected = false,
                    onDeleteVoteOption = {},
                )
                DateCard(
                    date = "No votes yet",
                    subtitle = "Be the first to vote",
                    voters = listOf(),
                    votePercentage = 0,
                    isSelected = false,
                    onDeleteVoteOption = {},
                )
            }
        }
    }
}
