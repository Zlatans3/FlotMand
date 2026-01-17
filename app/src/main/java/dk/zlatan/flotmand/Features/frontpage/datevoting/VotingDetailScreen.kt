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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dk.zlatan.flotmand.Features.frontpage.datevoting.ui.VotingListItem
import dk.zlatan.flotmand.design_system.theme.FlotMandTheme
import dk.zlatan.flotmand.model.DateVotingItem

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
            // Clear immediately to prevent re-navigation
            viewModel.clearNewVotingId()
            onVotingClick(votingId)
        }
    }

    // Ensure state is cleared when leaving composition
    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearNewVotingId()
        }
    }

    if (uiState.showCreateVotingDialog) {
        CreateVotingDialog(
            onDismiss = viewModel::dismissCreateVotingDialog,
            onConfirm = viewModel::createNewVoting
        )
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        DateVotingScreen(
            modifier = Modifier.padding(paddingValues),
            uiState = uiState,
            onVotingClick = onVotingClick,
            onCreateVoting = viewModel::showCreateVotingDialog
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

@Preview
@Composable
private fun DateVotingScreenPreview() {
    val mockVotings = DateVotingItem.mockDateVotingItemListCount(5, 5)

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