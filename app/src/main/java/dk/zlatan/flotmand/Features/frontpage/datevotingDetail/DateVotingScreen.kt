package dk.zlatan.flotmand.Features.frontpage.datevotingDetail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dk.zlatan.flotmand.Features.frontpage.datevotingDetail.ui.DateCard
import dk.zlatan.flotmand.design_system.theme.FlotMandTheme
import dk.zlatan.flotmand.model.DateOption
import dk.zlatan.flotmand.model.DateVoting
import dk.zlatan.flotmand.model.User
import java.time.LocalDate
import java.time.format.DateTimeFormatter


@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DateVotingDetailRoute(
    modifier: Modifier = Modifier,
    votingId: String,
    onDismiss: () -> Unit = {},
    viewModel: DateVotingViewModel = hiltViewModel<DateVotingViewModel, DateVotingViewModel.Factory>(
        key = votingId,
        creationCallback = { factory ->
            factory.create(votingId)
        }
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Show snackbar when there's a message
    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.dismissSnackbar()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Vælg dato",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        var showDatePicker by remember { mutableStateOf(false) }

        DateVotingDetailScreen(
            modifier = Modifier.padding(paddingValues),
            dateVoting = uiState.dateVoting,
            currentUserId = uiState.currentUserId,
            votersByUserId = uiState.votersByUserId,
            onVoteForDate = viewModel::onVoteForDate,
            onRemoveVote = viewModel::onRemoveVote,
            onAddDate = { showDatePicker = true },
            errorMessage = uiState.errorMessage,
            isCreator = uiState.dateVoting?.creatorId == uiState.currentUserId,
            onCreateEvent = {
                // Get the winning date from the voting
                val winningDate = uiState.dateVoting?.winningDate
                if (winningDate?.localDate != null) {
                    // TODO: Navigate to create event screen with the winning date pre-filled
                    // For now, we'll just delete the voting
                    viewModel.deleteVoting()
                } else {
                    // Show error if no winning date
                }
            },
            onDeleteVoting = viewModel::deleteVoting
        )

        // Date Picker Dialog
        if (showDatePicker) {
            val datePickerState = rememberDatePickerState()

            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                val selectedDate = java.time.Instant
                                    .ofEpochMilli(millis)
                                    .atZone(java.time.ZoneId.systemDefault())
                                    .toLocalDate()
                                viewModel.onAddDateOption(selectedDate)
                            }
                            showDatePicker = false
                        }
                    ) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Annuller")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}

@Composable
private fun DateVotingDetailScreen(
    modifier: Modifier = Modifier,
    dateVoting: DateVoting? = null,
    currentUserId: String = "",
    votersByUserId: Map<String, User> = emptyMap(),
    onVoteForDate: (LocalDate) -> Unit = {},
    onRemoveVote: (LocalDate) -> Unit = {},
    onAddDate: () -> Unit = {},
    errorMessage: String? = null,
    isCreator: Boolean = false,
    onCreateEvent: () -> Unit = {},
    onDeleteVoting: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Section
        VotingDetailsHeader(isCreator = isCreator)

        // Error message if present
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }

        // Voting Stats Section
        if (dateVoting != null) {
            VotingStatsSection(dateVoting = dateVoting)

            // Date Options Section
            VotingDatesSection(
                dateVoting = dateVoting,
                currentUserId = currentUserId,
                votersByUserId = votersByUserId,
                onVoteForDate = onVoteForDate,
                onRemoveVote = onRemoveVote
            )

            // Add Date button (always visible)
            AddDateButton(onAddDate = onAddDate)

            // Creator Actions Section (only for creator)
            if (isCreator && dateVoting.dateOptions.isNotEmpty()) {
                CreatorActionsSection(
                    onCreateEvent = onCreateEvent,
                    onDeleteVoting = onDeleteVoting
                )
            }
        }
    }
}

@Composable
private fun VotingDetailsHeader(isCreator: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = if (isCreator) "Min Afstemning" else "Afstemning",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "Stem på alle datoer hvor du er tilgængelig",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun VotingStatsSection(dateVoting: DateVoting) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        VotingStat(
            label = "Datoer",
            value = dateVoting.dateOptions.size.toString()
        )
        VotingStat(
            label = "Brugere Stemt",
            value = dateVoting.dateOptions
                .flatMap { it.votersId }
                .distinct()
                .size
                .toString()
        )
        VotingStat(
            label = "Status",
            value = if (dateVoting.status.name == "OPEN") "Åben" else "Lukket"
        )
    }
}

@Composable
private fun VotingStat(label: String, value: String) {
    Column(
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun VotingDatesSection(
    dateVoting: DateVoting,
    currentUserId: String,
    votersByUserId: Map<String, User> = emptyMap(),
    onVoteForDate: (LocalDate) -> Unit,
    onRemoveVote: (LocalDate) -> Unit = {}
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Tilgængelige datoer",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Sort date options by vote count (descending), then by date (ascending)
        val sortedDateOptions = dateVoting.dateOptions.sortedWith(
            compareByDescending<DateOption> { it.voteCount }
                .thenBy { it.localDate }
        )

        sortedDateOptions.forEach { dateOption ->
            // Animate with smooth slide in/out movements
            val dateKey = dateOption.localDate?.toString() ?: "unknown"
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(
                    animationSpec = tween(durationMillis = 400),
                    initialOffsetY = { -it }
                ),
                exit = slideOutVertically(
                    animationSpec = tween(durationMillis = 400),
                    targetOffsetY = { it }
                ),
                label = "dateCardSlide_$dateKey"
            ) {
                // Get actual user objects for voters, with fallback display names if data not loaded yet
                val participants = dateOption.votersId.mapNotNull { voterId ->
                    votersByUserId[voterId] ?: User(id = voterId, displayName = "Loading...")
                }

                val formatter = DateTimeFormatter.ofPattern("EEEE, MMM d'th'", java.util.Locale.ENGLISH)
                val dateString = dateOption.localDate?.format(formatter) ?: "Unknown date"

                val votingCount = dateOption.voteCount
                val subtitle = if (votingCount == 0) {
                    "Ingen stemmer endnu"
                } else if (votingCount == 1) {
                    "1 person stemte"
                } else {
                    "$votingCount personer stemte"
                }

                val isSelected = dateOption.votersId.contains(currentUserId)

                DateCard(
                    date = dateString,
                    subtitle = subtitle,
                    voters = participants,
                    votePercentage = dateVoting.getVotePercentage(dateOption),
                    isSelected = isSelected,
                    onClick = {
                        dateOption.localDate?.let { date ->
                            // Allow voting on multiple dates: toggle vote for each date independently
                            if (isSelected) {
                                onRemoveVote(date)
                            } else {
                                onVoteForDate(date)
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun AddDateButton(onAddDate: () -> Unit) {
    Row(
        modifier = Modifier
            .clickable(onClick = onAddDate)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = "Add date",
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Tilføj dato",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun CreatorActionsSection(
    onCreateEvent: () -> Unit,
    onDeleteVoting: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Creator Handlinger",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Create Event Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onCreateEvent)
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Create event",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(8.dp)
            )
            Column {
                Text(
                    text = "Opret Event",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Konverter denne afstemning til et event",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Delete Voting Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onDeleteVoting)
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Delete voting",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(8.dp)
            )
            Column {
                Text(
                    text = "Slet Afstemning",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    text = "Fjern denne afstemning permanent",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DateVotingDetailScreenPreview() {
    val mockDateOptions = listOf(
        DateOption(
            date = LocalDate.of(2026, 1, 23).toString(),
            votersId = listOf("user1", "user2", "user3")
        ),
        DateOption(
            date = LocalDate.of(2026, 1, 24).toString(),
            votersId = listOf("user4", "user5")
        ),
        DateOption(
            date = LocalDate.of(2026, 1, 25).toString(),
            votersId = listOf("user6")
        )
    )

    val mockDateVoting = DateVoting(
        votingId = "voting1",
        dateOptions = mockDateOptions
    )

    FlotMandTheme {
        DateVotingDetailScreen(
            modifier = Modifier,
            dateVoting = mockDateVoting
        )
    }
}