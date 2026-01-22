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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dk.zlatan.flotmand.Features.frontpage.datevotingDetail.ui.DateCard
import dk.zlatan.flotmand.Features.frontpage.datevotingDetail.ui.DeleteDialog
import dk.zlatan.flotmand.R
import dk.zlatan.flotmand.design_system.componenets.FmBackButton
import dk.zlatan.flotmand.design_system.componenets.ProfileImage
import dk.zlatan.flotmand.design_system.theme.FlotMandTheme
import dk.zlatan.flotmand.model.DateOption
import dk.zlatan.flotmand.model.DateVotingItem
import dk.zlatan.flotmand.model.User
import dk.zlatan.flotmand.util.DanishDateFormatter
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DateVotingDetailRoute(
    modifier: Modifier = Modifier,
    votingId: String,
    onDismiss: () -> Unit = {},
    onCreateEvent: (String) -> Unit = {},
    viewModel: DateVotingViewModel =
        hiltViewModel<DateVotingViewModel, DateVotingViewModel.Factory>(
            key = votingId,
            creationCallback = { factory ->
                factory.create(votingId)
            },
        ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var showDeleteDialog by remember { mutableStateOf(false) }

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
                        text = uiState.dateVotingItem?.name.orEmpty(),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                },
                navigationIcon = {
                    FmBackButton(
                        onBackClick = onDismiss,
                    )
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                    ),
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    ) { paddingValues ->
        var showDatePicker by remember { mutableStateOf(false) }

        DateVotingDetailScreen(
            modifier = Modifier.padding(paddingValues),
            dateVotingItem = uiState.dateVotingItem,
            currentUserId = uiState.currentUserId,
            votersByUserId = uiState.votersByUserId,
            onVoteForDate = viewModel::onVoteForDate,
            onRemoveVote = viewModel::onRemoveVote,
            onAddDate = { showDatePicker = true },
            errorMessage = uiState.errorMessage,
            publisher = uiState.publisherUser,
            isCreator = uiState.dateVotingItem?.creatorId == uiState.currentUserId,
            onCreateEvent = {
                // Navigate to AddEvent screen with the voting ID
                uiState.dateVotingItem?.votingId?.let { votingId ->
                    onCreateEvent(votingId)
                }
            },
            onShowDeleteDialog = {
                showDeleteDialog = true
            },
            onDeleteVoteOption = viewModel::onDeleteVotingOption,
        )

        if (showDeleteDialog) {
            DeleteDialog(
                onConfirmDelete = {
                    viewModel.deleteVoting()
                    showDeleteDialog = false
                    onDismiss()
                },
                onDismiss = {
                    showDeleteDialog = false
                },
            )
        }

        // Date Picker Dialog
        if (showDatePicker) {
            val datePickerState = rememberDatePickerState()

            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                val selectedDate =
                                    Instant
                                        .ofEpochMilli(millis)
                                        .atZone(ZoneId.systemDefault())
                                        .toLocalDate()
                                viewModel.onAddDateOption(selectedDate)
                            }
                            showDatePicker = false
                        },
                    ) {
                        Text(stringResource(R.string.ok))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                },
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}

@Composable
private fun DateVotingDetailScreen(
    modifier: Modifier = Modifier,
    dateVotingItem: DateVotingItem?,
    currentUserId: String,
    votersByUserId: Map<String, User>,
    onVoteForDate: (LocalDate) -> Unit,
    onRemoveVote: (LocalDate) -> Unit,
    publisher: User? = null,
    onAddDate: () -> Unit,
    errorMessage: String?,
    isCreator: Boolean,
    onCreateEvent: () -> Unit,
    onShowDeleteDialog: () -> Unit,
    onDeleteVoteOption: (DateOption) -> Unit,
) {
    val haptic = LocalHapticFeedback.current

    Column(
        modifier =
            modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Header Section
        VotingDetailsHeader(
            isCreator = isCreator,
            user = publisher,
        )

        // Error message if present
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
        }

        // Voting Stats Section
        if (dateVotingItem != null) {
            VotingStatsSection(dateVotingItem = dateVotingItem)

            // Date Options Section
            VotingDatesSection(
                dateVotingItem = dateVotingItem,
                currentUserId = currentUserId,
                votersByUserId = votersByUserId,
                onVoteForDate = { date ->
                    haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                    onVoteForDate(date)
                },
                onRemoveVote = { date ->
                    haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                    onRemoveVote(date)
                },
                onDeleteVoteOption = onDeleteVoteOption,
            )

            AddDateButton(onAddDate = onAddDate)

            // Creator Actions Section (only for creator)
            if (isCreator && dateVotingItem.dateOptions.isNotEmpty()) {
                CreatorActionsSection(
                    onCreateEvent = onCreateEvent,
                    onShowDeleteDialog = onShowDeleteDialog,
                )
            }
        }
    }
}

@Composable
private fun VotingDetailsHeader(
    isCreator: Boolean,
    user: User?,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = if (isCreator) stringResource(R.string.my_voting) else stringResource(R.string.voting),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Text(
            text = stringResource(R.string.vote_on_dates),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (user != null && !isCreator) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ProfileImage(
                    profilePic = user.photoUrl,
                    profileSize = 24.dp,
                    userName = user.displayName,
                )

                val creatorText =
                    buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(fontWeight = FontWeight.Light),
                        ) {
                            append(stringResource(R.string.created_by))
                        }
                        withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold)) {
                            append(user.getFirstName())
                        }
                    }
                Text(
                    text = creatorText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun VotingStatsSection(dateVotingItem: DateVotingItem) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        VotingStat(
            label = stringResource(R.string.dates),
            value = dateVotingItem.dateOptions.size.toString(),
        )
        VotingStat(
            label = stringResource(R.string.users_voted),
            value =
                dateVotingItem.dateOptions
                    .flatMap { it.votersId }
                    .distinct()
                    .size
                    .toString(),
        )
        VotingStat(
            label = stringResource(R.string.status),
            value = if (dateVotingItem.status.name == "OPEN") stringResource(R.string.open) else stringResource(R.string.closed),
        )
    }
}

@Composable
private fun VotingStat(
    label: String,
    value: String,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun VotingDatesSection(
    dateVotingItem: DateVotingItem,
    currentUserId: String,
    votersByUserId: Map<String, User> = emptyMap(),
    onVoteForDate: (LocalDate) -> Unit,
    onRemoveVote: (LocalDate) -> Unit,
    onDeleteVoteOption: (DateOption) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(R.string.available_dates),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )

        // Sort date options by vote count (descending), then by date (ascending)
        val sortedDateOptions =
            dateVotingItem.dateOptions.sortedWith(
                compareByDescending<DateOption> { it.voteCount }
                    .thenBy { it.localDate },
            )

        sortedDateOptions.forEach { dateOption ->
            // Animate with smooth slide in/out movements
            val dateKey = dateOption.localDate?.toString() ?: "unknown"
            AnimatedVisibility(
                visible = true,
                enter =
                    slideInVertically(
                        animationSpec = tween(durationMillis = 400),
                        initialOffsetY = { -it },
                    ),
                exit =
                    slideOutVertically(
                        animationSpec = tween(durationMillis = 400),
                        targetOffsetY = { it },
                    ),
                label = "dateCardSlide_$dateKey",
            ) {
                // Get actual user objects for voters, with fallback display names if data not loaded yet
                val participants =
                    dateOption.votersId.map { voterId ->
                        votersByUserId[voterId] ?: User(id = voterId, displayName = "Loading...")
                    }

                val dateString =
                    dateOption.localDate?.let {
                        DanishDateFormatter.formatDateDanishWithWeekday(it)
                    } ?: stringResource(R.string.unknown_date)

                val votingCount = dateOption.voteCount
                val subtitle =
                    when (votingCount) {
                        0 -> stringResource(R.string.no_votes_yet)
                        1 -> stringResource(R.string.one_person_voted)
                        else -> stringResource(R.string.n_persons_voted, votingCount)
                    }

                val isSelected = dateOption.votersId.contains(currentUserId)

                DateCard(
                    date = dateString,
                    subtitle = subtitle,
                    voters = participants,
                    votePercentage = dateVotingItem.getVotePercentage(dateOption),
                    isSelected = isSelected,
                    onDeleteVoteOption = {
                        onDeleteVoteOption(dateOption)
                    },
                    onClick = {
                        dateOption.localDate?.let { date ->
                            // Allow voting on multiple dates: toggle vote for each date independently
                            if (isSelected) {
                                onRemoveVote(date)
                            } else {
                                onVoteForDate(date)
                            }
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun AddDateButton(onAddDate: () -> Unit) {
    Row(
        modifier =
            Modifier
                .clickable(onClick = onAddDate)
                .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = stringResource(R.string.add_date_content_description),
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = stringResource(R.string.add_date),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun CreatorActionsSection(
    onCreateEvent: () -> Unit,
    onShowDeleteDialog: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(R.string.actions),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )

        // Create Event Button
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onCreateEvent)
                    .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = stringResource(R.string.create_event_content_description),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(8.dp),
            )
            Column {
                Text(
                    text = stringResource(R.string.create_event),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = stringResource(R.string.convert_to_event),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // Delete Voting Button
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onShowDeleteDialog)
                    .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = stringResource(R.string.delete_voting_content_description),
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(8.dp),
            )
            Column {
                Text(
                    text = stringResource(R.string.delete_voting),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error,
                )
                Text(
                    text = stringResource(R.string.remove_voting_permanently),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DateVotingDetailScreenPreview() {
    val mockDateOptions =
        listOf(
            DateOption(
                date = LocalDate.of(2026, 1, 23).toString(),
                votersId = listOf("user1", "user2", "user3"),
            ),
            DateOption(
                date = LocalDate.of(2026, 1, 24).toString(),
                votersId = listOf("user4", "user5"),
            ),
            DateOption(
                date = LocalDate.of(2026, 1, 25).toString(),
                votersId = listOf("user6"),
            ),
        )

    val mockDateVotingItem =
        DateVotingItem(
            votingId = "voting1",
            creatorId = "1",
            dateOptions = mockDateOptions,
        )

    FlotMandTheme {
        DateVotingDetailScreen(
            modifier = Modifier,
            dateVotingItem = mockDateVotingItem,
            currentUserId = "",
            votersByUserId = emptyMap(),
            onVoteForDate = { },
            onRemoveVote = { },
            onAddDate = { },
            errorMessage = null,
            isCreator = true,
            onCreateEvent = { },
            onShowDeleteDialog = { },
            onDeleteVoteOption = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DateVotingDetailIsNotCreatorScreenPreview() {
    val mockDateOptions =
        listOf(
            DateOption(
                date = LocalDate.of(2026, 1, 23).toString(),
                votersId = listOf("user1", "user2", "user3"),
            ),
            DateOption(
                date = LocalDate.of(2026, 1, 24).toString(),
                votersId = listOf("user4", "user5"),
            ),
            DateOption(
                date = LocalDate.of(2026, 1, 25).toString(),
                votersId = listOf("user6"),
            ),
        )

    val mockDateVotingItem =
        DateVotingItem(
            votingId = "voting1",
            creatorId = "1",
            dateOptions = mockDateOptions,
        )

    FlotMandTheme {
        DateVotingDetailScreen(
            modifier = Modifier,
            dateVotingItem = mockDateVotingItem,
            currentUserId = "",
            votersByUserId = emptyMap(),
            onVoteForDate = { },
            onRemoveVote = { },
            publisher = User.mockUserWithCounter(1).first(),
            onAddDate = { },
            errorMessage = null,
            isCreator = false,
            onCreateEvent = { },
            onShowDeleteDialog = { },
            onDeleteVoteOption = {}
        )
    }
}
