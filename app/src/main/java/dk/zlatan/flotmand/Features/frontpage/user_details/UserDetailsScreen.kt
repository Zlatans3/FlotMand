package dk.zlatan.flotmand.Features.frontpage.user_details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dk.zlatan.flotmand.R
import dk.zlatan.flotmand.design_system.componenets.EventCard
import dk.zlatan.flotmand.design_system.componenets.FmBackButton
import dk.zlatan.flotmand.design_system.componenets.PredictiveBackScaleContainer
import dk.zlatan.flotmand.design_system.componenets.ProfileImage
import dk.zlatan.flotmand.design_system.componenets.spacers.HSpacer
import dk.zlatan.flotmand.design_system.componenets.spacers.VSpacer
import dk.zlatan.flotmand.design_system.theme.FlotMandTheme
import dk.zlatan.flotmand.model.Event
import dk.zlatan.flotmand.model.User
import dk.zlatan.flotmand.util.DanishDateFormatter

@Composable
fun UserDetailsScreenRoute(
    userId: String,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onEventClick: (String) -> Unit = {},
    viewModel: UserDetailsViewModel =
        hiltViewModel<UserDetailsViewModel, UserDetailsViewModel.Factory>(
            key = userId,
            creationCallback = { factory -> factory.create(userId) },
        ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isSavingBio by viewModel.isSavingBio.collectAsStateWithLifecycle()

    PredictiveBackScaleContainer(modifier = modifier) {
        UserDetailsScreen(
            uiState = uiState,
            isSavingBio = isSavingBio,
            onBackClick = onDismiss,
            onEventClick = onEventClick,
            onSaveBio = viewModel::saveBio,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun UserDetailsScreen(
    uiState: UserDetailsUiState,
    isSavingBio: Boolean = false,
    onBackClick: () -> Unit = {},
    onEventClick: (String) -> Unit = {},
    onSaveBio: (String) -> Unit = {},
) {
    var showEditBioDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.user_details_title)) },
                navigationIcon = { FmBackButton(onBackClick = onBackClick) },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                    ),
            )
        },
    ) { paddingValues ->
        // Only consume the top inset — the full padding values include the bottom
        // nav-bar inset, which would leave a dead gap under the scrolling content.
        val topPadding = paddingValues.calculateTopPadding()
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = topPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.user == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = topPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.user_details_user_not_found),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            else -> {
                UserDetailsContent(
                    modifier = Modifier.padding(top = topPadding),
                    user = uiState.user,
                    isOwnProfile = uiState.isOwnProfile,
                    eventsHosted = uiState.eventsHosted,
                    eventsAttended = uiState.eventsAttended,
                    previousEvents = uiState.previousEvents,
                    onEventClick = onEventClick,
                    onEditBioClick = { showEditBioDialog = true },
                )
            }
        }
    }

    if (showEditBioDialog && uiState.user != null) {
        EditBioDialog(
            initialBio = uiState.user.bio,
            isSaving = isSavingBio,
            onDismiss = { showEditBioDialog = false },
            onSave = { newBio ->
                onSaveBio(newBio)
                showEditBioDialog = false
            },
        )
    }
}

@Composable
private fun UserDetailsContent(
    modifier: Modifier = Modifier,
    user: User,
    isOwnProfile: Boolean,
    eventsHosted: Int,
    eventsAttended: Int,
    previousEvents: List<Event>,
    onEventClick: (String) -> Unit,
    onEditBioClick: () -> Unit,
) {
    val danishFormatter = remember { DanishDateFormatter.getDanishDateFormatter("E 'd.' d MMM") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        VSpacer(16.dp)

        ProfileImage(
            profilePic = user.photoUrl,
            userName = user.displayName,
            profileSize = 96.dp,
        )

        VSpacer(12.dp)

        Text(
            text = user.displayName,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )

        VSpacer(8.dp)

        BioSection(
            bio = user.bio,
            isOwnProfile = isOwnProfile,
            onEditBioClick = onEditBioClick,
        )

        VSpacer(20.dp)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StatBox(
                modifier = Modifier.weight(1f),
                count = eventsHosted,
                label = stringResource(R.string.user_details_hosted_events),
            )
            StatBox(
                modifier = Modifier.weight(1f),
                count = eventsAttended,
                label = stringResource(R.string.user_details_attended_events),
            )
        }

        VSpacer(28.dp)

        Text(
            text = stringResource(R.string.user_details_previous_events),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.fillMaxWidth(),
        )

        VSpacer(12.dp)

        if (previousEvents.isEmpty()) {
            Text(
                text = stringResource(R.string.user_details_no_previous_events),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 20.dp),
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                previousEvents.forEach { event ->
                    val formattedDate = event.eventDate
                        ?.format(danishFormatter)
                        ?.replaceFirstChar { it.uppercase() }
                        .orEmpty()
                    EventCard(
                        userProfilePic = user.photoUrl,
                        userName = user.displayName,
                        eventName = event.eventName.orEmpty(),
                        eventDate = formattedDate,
                        eventTime = event.eventStartTime?.toString().orEmpty(),
                        onClick = { event.eventId?.let(onEventClick) },
                    )
                }
            }
        }

        VSpacer(40.dp)
    }
}

@Composable
private fun BioSection(
    bio: String,
    isOwnProfile: Boolean,
    onEditBioClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            text = bio.ifBlank {
                if (isOwnProfile) stringResource(R.string.user_details_bio_placeholder) else stringResource(R.string.user_details_no_bio)
            },
            style = MaterialTheme.typography.bodyMedium,
            color =
                if (bio.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f, fill = false),
        )
        if (isOwnProfile) {
            HSpacer(8.dp)
            FilledTonalIconButton(
                onClick = onEditBioClick,
                modifier = Modifier.size(28.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = stringResource(R.string.user_details_edit_bio),
                    modifier = Modifier.size(14.dp),
                )
            }
        }
    }
}

@Composable
private fun StatBox(
    modifier: Modifier = Modifier,
    count: Int,
    label: String,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "$count",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            VSpacer(4.dp)
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun EditBioDialog(
    initialBio: String,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    var bioInput by rememberSaveable { mutableStateOf(initialBio) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.user_details_edit_bio)) },
        text = {
            OutlinedTextField(
                value = bioInput,
                onValueChange = { bioInput = it.take(User.BIO_MAX_LENGTH) },
                placeholder = { Text(stringResource(R.string.user_details_bio_placeholder)) },
                supportingText = {
                    Text(
                        text =
                            stringResource(
                                R.string.user_details_bio_char_count,
                                bioInput.length,
                                User.BIO_MAX_LENGTH,
                            ),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End,
                    )
                },
                minLines = 3,
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(bioInput) },
                enabled = !isSaving && bioInput.trim() != initialBio,
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp))
                } else {
                    Text(stringResource(R.string.save))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun UserDetailsScreenPreview() {
    FlotMandTheme {
        UserDetailsScreen(
            uiState =
                UserDetailsUiState(
                    user =
                        User(
                            id = "u1",
                            displayName = "Zlatan Stadler",
                            bio = "Glad amatørkok der elsker at samle vennerne om god mad.",
                        ),
                    isLoading = false,
                    isOwnProfile = true,
                    eventsHosted = 4,
                    eventsAttended = 11,
                    previousEvents = Event.previewEvents(2),
                ),
        )
    }
}
