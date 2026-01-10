package dk.zlatan.flotmand.Features.frontpage.datevoting

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dk.zlatan.flotmand.design_system.theme.FlotMandTheme
import dk.zlatan.flotmand.model.DateVoting
import dk.zlatan.flotmand.model.VotingStatus
import java.time.format.DateTimeFormatter

@Composable
internal fun DateVotingRoute(
    modifier: Modifier = Modifier,
    onVotingClick: (String) -> Unit,
    viewModel: DateVotingListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle snackbar messages
    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.dismissSnackbar()
        }
    }

    // Navigate to new voting when created
    LaunchedEffect(uiState.newVotingId) {
        uiState.newVotingId?.let { votingId ->
            onVotingClick(votingId)
            viewModel.clearNewVotingId()
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        DateVotingScreen(
            modifier = Modifier.padding(paddingValues),
            uiState = uiState,
            onVotingClick = onVotingClick,
            onCreateVoting = viewModel::createNewVoting
        )
    }
}

@Composable
internal fun DateVotingScreen(
    modifier: Modifier = Modifier,
    uiState: DateVotingListUiState = DateVotingListUiState(),
    onVotingClick: (String) -> Unit = {},
    onCreateVoting: () -> Unit = {}
) {
    Box(modifier = modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator()
                        Text(
                            text = "Henter afstemninger...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
                ) {
                    item {
                        VotingListHeader(onCreateVoting = onCreateVoting)
                    }

                    items(uiState.votings) { voting ->
                        VotingListItem(
                            voting = voting,
                            isCreator = voting.creatorId == uiState.currentUserId,
                            onClick = {
                                voting.votingId?.let { onVotingClick(it) }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VotingListHeader(onCreateVoting: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Afstemninger",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onCreateVoting)
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Create voting",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(8.dp)
            )
            Text(
                text = "Opret ny afstemning",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun VotingListItem(
    voting: DateVoting,
    isCreator: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Filled.DateRange,
                    contentDescription = "Voting",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(8.dp)
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = if (isCreator) "Min afstemning" else "Afstemning",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "${voting.dateOptions.size} datoer • ${voting.totalVotes} stemmer",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = formatCreatedDate(voting.createdAtString),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Status badge
            StatusBadge(status = voting.status.name)
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
    val (backgroundColor, textColor) = when (status) {
        "OPEN" -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = Modifier
            .background(backgroundColor, shape = RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (status == "OPEN") "Åben" else "Lukket",
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun formatCreatedDate(dateString: String?): String {
    return if (dateString != null) {
        try {
            val date = java.time.LocalDateTime.parse(dateString)
            val now = java.time.LocalDateTime.now()

            val days = java.time.temporal.ChronoUnit.DAYS.between(date.toLocalDate(), now.toLocalDate()).toInt()

            when {
                days == 0 -> "I dag"
                days == 1 -> "I går"
                days < 7 -> "For $days dage siden"
                else -> date.toLocalDate().toString()
            }
        } catch (_: Exception) {
            "Ukendt dato"
        }
    } else {
        "Ukendt dato"
    }
}

@Preview
@Composable
private fun DateVotingScreenPreview() {
    val mockVotings = listOf(
        DateVoting(
            votingId = "1",
            creatorId = "user1",
            status = VotingStatus.OPEN,
            dateOptions = emptyList(),
            createdAtString = java.time.LocalDateTime.now().toString()
        ),
        DateVoting(
            votingId = "2",
            creatorId = "user2",
            status = VotingStatus.OPEN,
            dateOptions = emptyList(),
            createdAtString = java.time.LocalDateTime.now().minusDays(2).toString()
        )
    )

    FlotMandTheme {
        DateVotingScreen(
            uiState = DateVotingListUiState(
                votings = mockVotings,
                currentUserId = "user1",
                isLoading = false
            ),
            onVotingClick = {},
            onCreateVoting = {}
        )
    }
}