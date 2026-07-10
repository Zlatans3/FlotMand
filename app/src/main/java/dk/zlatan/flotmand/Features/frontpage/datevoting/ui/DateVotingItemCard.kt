package dk.zlatan.flotmand.Features.frontpage.datevoting.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.HowToVote
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dk.zlatan.flotmand.R
import dk.zlatan.flotmand.model.DateVotingItem
import dk.zlatan.flotmand.util.CreatedDateLabelText
import dk.zlatan.flotmand.util.formatCreatedDateString

@Composable
fun votingListItem(
    voting: DateVotingItem,
    isCreator: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            // surface == background in the Harvest theme, so cards need a
            // container tone to read as cards rather than bare shadows.
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f),
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                        .padding(10.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.HowToVote,
                        contentDescription = stringResource(R.string.voting),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp),
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    if (isCreator) {
                        Text(
                            text = stringResource(R.string.my_voting),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }

                    Text(
                        text = voting.name?.takeIf { it.isNotBlank() }
                            ?: stringResource(R.string.voting),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Text(
                        text = pluralStringResource(R.plurals.date_options_count, voting.dateOptions.size, voting.dateOptions.size) +
                            " • " +
                            pluralStringResource(R.plurals.users_voted_count, voting.usersVoted, voting.usersVoted),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    CreatedDateLabelText(label = formatCreatedDateString(voting.createdAtString))
                }
            }

            VotingStatusBadge(isOpen = voting.status.name == "OPEN")
        }
    }
}

/** Capsule-with-dot badge matching the design system's StatusBadge language. */
@Composable
private fun VotingStatusBadge(isOpen: Boolean) {
    val dotColor =
        if (isOpen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline

    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .padding(end = 6.dp)
                    .clip(CircleShape)
                    .background(dotColor)
                    .padding(4.dp),
            )
            Text(
                text = if (isOpen) stringResource(R.string.open) else stringResource(R.string.closed),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview
@Composable
private fun VotingListItemPreview() {
    votingListItem(
        modifier = Modifier,
        voting = DateVotingItem.mockDateVotingItemCount(1),
        isCreator = true,
        onClick = {},
    )
}
