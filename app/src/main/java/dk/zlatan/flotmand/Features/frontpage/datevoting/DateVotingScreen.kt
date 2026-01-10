package dk.zlatan.flotmand.Features.frontpage.datevoting

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
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
import dk.zlatan.flotmand.Features.frontpage.datevoting.ui.DateCard
import dk.zlatan.flotmand.design_system.theme.FlotMandTheme
import dk.zlatan.flotmand.model.DateOption
import dk.zlatan.flotmand.model.DateVoting
import dk.zlatan.flotmand.model.User
import java.time.LocalDate
import java.time.format.DateTimeFormatter


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateVotingRoute(
    modifier: Modifier = Modifier,
    eventId: String? = null,
    onDismiss: () -> Unit = {},
    viewModel: DateVotingViewModel = hiltViewModel()
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

        DateVotingScreen(
            modifier = Modifier.padding(paddingValues),
            dateVoting = uiState.dateVoting,
            currentUserId = uiState.currentUserId,
            onVoteForDate = viewModel::onVoteForDate,
            onAddDate = { showDatePicker = true },
            eventId = eventId,
            errorMessage = uiState.errorMessage
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
private fun DateVotingScreen(
    modifier: Modifier = Modifier,
    dateVoting: DateVoting? = null,
    currentUserId: String = "",
    onVoteForDate: (LocalDate) -> Unit = {},
    onAddDate: () -> Unit = {},
    eventId: String? = null,
    errorMessage: String? = null
) {
    Column(
        modifier = modifier
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Vælg dato for arrangement",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "Her kan du stemme på datoer for kommende arrangementer.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Show error message if present
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }

        // Display date cards
        if (dateVoting != null) {
            dateVoting.dateOptions.forEach { dateOption ->
                val participants = dateOption.votersId.map { voterId ->
                    User(id = voterId, displayName = "Voter")
                }

                val formatter = DateTimeFormatter.ofPattern("EEEE, MMM d'th'", java.util.Locale.ENGLISH)
                val dateString = dateOption.localDate?.format(formatter) ?: "Unknown date"

                val votingCount = dateOption.voteCount
                val subtitle = if (votingCount == 0) {
                    "No votes yet"
                } else if (votingCount == 1) {
                    "1 friend voted"
                } else {
                    "$votingCount friends voted"
                }

                val isSelected = dateOption.votersId.contains(currentUserId)

                DateCard(
                    date = dateString,
                    subtitle = subtitle,
                    voters = participants,
                    votePercentage = dateVoting.getVotePercentage(dateOption),
                    isSelected = isSelected,
                    onClick = {
                        dateOption.localDate?.let { onVoteForDate(it) }
                    }
                )
            }
        }

        // Add Date button
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
}

@Preview(showBackground = true)
@Composable
private fun DateVotingScreenPreview() {
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
        DateVotingScreen(
            modifier = Modifier,
            dateVoting = mockDateVoting
        )
    }
}