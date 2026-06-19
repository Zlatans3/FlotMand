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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dk.zlatan.flotmand.Features.frontpage.datevoting.ui.votingListItem
import dk.zlatan.flotmand.design_system.componenets.FmBackButton
import dk.zlatan.flotmand.design_system.componenets.topappbar.FmTopAppBar
import dk.zlatan.flotmand.design_system.theme.FlotMandTheme
import dk.zlatan.flotmand.model.DateVotingItem
import dk.zlatan.flotmand.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DateVotingRoute(
    modifier: Modifier = Modifier,
    onVotingClick: (String) -> Unit,
    onDismiss: () -> Unit,
    showBackButton: Boolean = true,
    viewModel: DateVotingListViewModel = hiltViewModel(),
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
            onConfirm = viewModel::createNewVoting,
        )
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            FmTopAppBar(
                containerColor = MaterialTheme.colorScheme.surface,
                leadingIcon = {
                    if (showBackButton) {
                        FmBackButton(
                            onBackClick = onDismiss,
                        )
                    }
                },
                textContent = {
                    Text(
                        text = stringResource(R.string.voting_list_title),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        DateVotingScreen(
            modifier = Modifier.padding(paddingValues),
            uiState = uiState,
            onVotingClick = onVotingClick,
            onCreateVoting = viewModel::showCreateVotingDialog,
        )
    }
}

@Composable
internal fun DateVotingScreen(
    modifier: Modifier = Modifier,
    uiState: DateVotingListUiState = DateVotingListUiState(),
    onVotingClick: (String) -> Unit = {},
    onCreateVoting: () -> Unit = {},
) {
    Box(modifier = modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        CircularProgressIndicator()
                        Text(
                            text = stringResource(R.string.voting_list_loading),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding =
                        androidx.compose.foundation.layout
                            .PaddingValues(16.dp),
                ) {
                    item {
                        VotingListHeader(onCreateVoting = onCreateVoting)
                    }

                    if (uiState.votings.isEmpty()) {
                        item {
                            val composition by rememberLottieComposition(
                                LottieCompositionSpec.RawRes(R.raw.three_cut),
                            )
                            Box(
                                modifier = Modifier
                                    .fillParentMaxHeight(0.8f)
                                    .fillMaxWidth(),
                            ) {
                                // Large poster-style text anchored to upper-right
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(top = 24.dp, end = 8.dp),
                                    horizontalAlignment = Alignment.End,
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    Text(
                                        text = stringResource(R.string.voting_list_empty),
                                        style = MaterialTheme.typography.displaySmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.End,
                                    )
                                    Text(
                                        text = stringResource(R.string.voting_list_empty_subtitle),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.End,
                                    )
                                }
                                // Cozy animation anchored to bottom-left
                                LottieAnimation(
                                    composition = composition,
                                    iterations = LottieConstants.IterateForever,
                                    modifier = Modifier
                                        .size(240.dp)
                                        .align(Alignment.BottomStart),
                                )
                            }
                        }
                    } else {
                        items(uiState.votings) { voting ->
                            votingListItem(
                                voting = voting,
                                isCreator = voting.creatorId == uiState.currentUserId,
                                onClick = {
                                    voting.votingId?.let { onVotingClick(it) }
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VotingListHeader(onCreateVoting: () -> Unit) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onCreateVoting)
                .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = stringResource(R.string.create_voting_content_description),
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(8.dp),
        )
        Text(
            text = stringResource(R.string.create_voting_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Preview
@Composable
private fun DateVotingScreenPreview() {
    val mockVotings = DateVotingItem.mockDateVotingItemListCount(5, 5)

    FlotMandTheme {
        DateVotingScreen(
            uiState =
                DateVotingListUiState(
                    votings = mockVotings,
                    currentUserId = "user1",
                    isLoading = false,
                ),
            onVotingClick = {},
            onCreateVoting = {},
        )
    }
}
