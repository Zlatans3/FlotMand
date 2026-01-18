package dk.zlatan.flotmand.Features.my_events.add_new_event

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dk.zlatan.flotmand.Features.my_events.add_new_event.ui.AddressAutocompleteDropdown
import dk.zlatan.flotmand.Features.my_events.add_new_event.ui.EventTextField
import dk.zlatan.flotmand.design_system.componenets.spacers.VSpacer
import dk.zlatan.flotmand.design_system.theme.FlotMandTheme
import dk.zlatan.flotmand.model.AddressPrediction
import dk.zlatan.flotmand.model.Event
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddEventScreenRoute(
    modifier: Modifier = Modifier,
    votingId: String? = null,
    viewModel: AddEventViewModel =
        hiltViewModel<AddEventViewModel, AddEventViewModel.Factory>(
            key = votingId ?: "new_event",
            creationCallback = { factory ->
                factory.create(votingId)
            },
        ),
    onDismiss: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val density = LocalDensity.current
    val imeHeight = WindowInsets.ime.getBottom(density)
    val isKeyboardOpen = imeHeight > 0
    val focusManager = LocalFocusManager.current

    // Navigate back when event is created successfully, then reset state
    LaunchedEffect(uiState.isEventCreated) {
        if (uiState.isEventCreated) {
            onDismiss()
            // Reset state after dismissing so it's clean for next time
            viewModel.resetState()
        }
    }

    // Show error message in snackbar
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Opret nyt event",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Luk",
                        )
                    }
                },
                actions = {
                    // Show "Opret" button in header when keyboard is open
                    AnimatedVisibility(
                        visible = isKeyboardOpen,
                        enter =
                            slideInHorizontally(
                                initialOffsetX = { fullWidth -> fullWidth },
                                animationSpec =
                                    spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow,
                                    ),
                            ) +
                                fadeIn(
                                    animationSpec = spring(stiffness = Spring.StiffnessLow),
                                ),
                        exit =
                            slideOutHorizontally(
                                targetOffsetX = { fullWidth -> fullWidth },
                                animationSpec =
                                    spring(
                                        dampingRatio = Spring.DampingRatioNoBouncy,
                                        stiffness = Spring.StiffnessMedium,
                                    ),
                            ) + fadeOut(),
                    ) {
                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                viewModel.createEvent()
                            },
                            enabled = !uiState.isLoading,
                            modifier = Modifier.padding(end = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary,
                                ),
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp,
                                )
                            } else {
                                Text(
                                    text = "Opret",
                                    style = MaterialTheme.typography.labelLarge,
                                    modifier = Modifier.padding(vertical = 4.dp),
                                )
                            }
                        }
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
            )
        },
    ) { paddingValues ->
        AddEventScreenContent(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(
                        start = paddingValues.calculateLeftPadding(androidx.compose.ui.unit.LayoutDirection.Ltr),
                        top = paddingValues.calculateTopPadding(),
                        end = paddingValues.calculateRightPadding(androidx.compose.ui.unit.LayoutDirection.Rtl),
                        bottom = 0.dp,
                    ),
            uiState = uiState,
            onEventNameChange = viewModel::onEventNameChange,
            onLocationChange = viewModel::onLocationChange,
            onEventDateChange = viewModel::onEventDateChange,
            onDescriptionChange = viewModel::onDescriptionChange,
            onEventTimeChange = viewModel::onEventTimeChange,
            onAddressSelected = viewModel::onAddressSelected,
            onClearPredictions = viewModel::clearAddressPredictions,
            onCreateEvent = viewModel::createEvent,
            isKeyboardOpen = isKeyboardOpen,
            focusManager = focusManager,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEventScreenContent(
    modifier: Modifier = Modifier,
    uiState: AddEventUiState,
    onEventNameChange: (String) -> Unit,
    onLocationChange: (TextFieldValue) -> Unit,
    onEventDateChange: (LocalDate) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onEventTimeChange: (LocalTime) -> Unit,
    onAddressSelected: (AddressPrediction) -> Unit,
    onClearPredictions: () -> Unit,
    focusManager: FocusManager,
    onCreateEvent: () -> Unit,
    isKeyboardOpen: Boolean,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    LaunchedEffect(isKeyboardOpen) {
        if (!isKeyboardOpen) {
            // IME was open and now closed — remove focus from any TextField
            focusManager.clearFocus()
        }
    }
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val locationFieldRequester = remember { BringIntoViewRequester() }

    // Keep location field in view when predictions appear or field is focused
    LaunchedEffect(uiState.addressPredictions.isNotEmpty(), uiState.locationTextFieldValue.text) {
        if (uiState.addressPredictions.isNotEmpty() || uiState.locationTextFieldValue.text.isNotEmpty()) {
            coroutineScope.launch {
                locationFieldRequester.bringIntoView()
            }
        }
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                    })
                }.verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
                .imePadding(),
        verticalArrangement = Arrangement.Top,
    ) {
        VSpacer(20.dp)

        // Event Name
        EventTextField(
            label = "Event navn",
            value = uiState.event.eventName.orEmpty(),
            onValueChange = onEventNameChange,
            placeholder = "Fx. Middag hos Gustav",
            singleLine = true,
            maxChar = 100,
            maxLines = 1,
        )

        VSpacer(16.dp)

        // Description (optional) directly under name, larger multi-line
        EventTextField(
            label = "Beskrivelse (valgfrit)",
            value = uiState.event.description.orEmpty(),
            onValueChange = { desc -> onDescriptionChange(desc) },
            placeholder = "Tilføj detaljer om eventet",
            singleLine = false,
            maxChar = 2000,
            minLines = 3,
            maxLines = 6,
        )

        VSpacer(20.dp)

        // Location with Autocomplete
        Box(
            modifier =
                Modifier
                    .zIndex(1f)
                    .bringIntoViewRequester(locationFieldRequester)
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused || focusState.hasFocus) {
                            coroutineScope.launch {
                                locationFieldRequester.bringIntoView()
                            }
                        }
                    },
        ) {
            Column {
                EventTextField(
                    label = "Lokation",
                    value = uiState.locationTextFieldValue,
                    onValueChange = onLocationChange,
                    placeholder = "Fx. Flotmand alle 4, København",
                    trailingIcon = {
                        IconButton(onClick = { /* optional: open map picker in future */ }) {
                            Icon(imageVector = Icons.Filled.Place, contentDescription = "Lokation")
                        }
                    },
                )

                // Autocomplete dropdown
                AddressAutocompleteDropdown(
                    predictions = uiState.addressPredictions,
                    isLoading = uiState.isLoadingPredictions,
                    onPredictionSelected = { prediction ->
                        onAddressSelected(prediction)
                        onClearPredictions()
                    },
                )
            }
        }

        VSpacer(20.dp)

        // Date & Time on the same line
        Row(modifier = Modifier.fillMaxWidth()) {
            EventTextField(
                label = "Dato",
                value =
                    uiState.event.eventDate?.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                        ?: "",
                onValueChange = { }, // Read-only
                placeholder = "Vælg dato",
                modifier = Modifier.weight(1f),
                onClick = {
                    focusManager.clearFocus()
                    showDatePicker = true
                },
                trailingIcon = {
                    IconButton(onClick = {
                        focusManager.clearFocus()
                        showDatePicker = true
                    }) {
                        Icon(imageVector = Icons.Filled.Today, contentDescription = "Vælg dato")
                    }
                },
            )

            Spacer(modifier = Modifier.size(12.dp))

            EventTextField(
                label = "Tidspunkt",
                value =
                    uiState.event.eventStartTime?.format(DateTimeFormatter.ofPattern("HH:mm"))
                        ?: "",
                onValueChange = { }, // Read-only
                placeholder = "tidspunkt",
                modifier = Modifier.weight(1f),
                singleLine = true,
                onClick = {
                    focusManager.clearFocus()
                    showTimePicker = true
                },
                trailingIcon = {
                    IconButton(onClick = {
                        focusManager.clearFocus()
                        showTimePicker = true
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Schedule,
                            contentDescription = "Vælg tidspunkt",
                        )
                    }
                },
            )
        }

        VSpacer(32.dp)

        // DatePicker Dialog
        if (showDatePicker) {
            val datePickerState =
                rememberDatePickerState(
                    initialSelectedDateMillis =
                        uiState.event.eventDate
                            ?.atStartOfDay(ZoneId.systemDefault())
                            ?.toInstant()
                            ?.toEpochMilli(),
                )

            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val localDate =
                                Instant
                                    .ofEpochMilli(millis)
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate()
                            onEventDateChange(localDate)
                        }
                        showDatePicker = false
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Annuller")
                    }
                },
            ) {
                DatePicker(state = datePickerState)
            }
        }

        // TimePicker Dialog
        if (showTimePicker) {
            val timePickerState =
                rememberTimePickerState(
                    initialHour = uiState.event.eventStartTime?.hour ?: 18,
                    initialMinute = uiState.event.eventStartTime?.minute ?: 0,
                    is24Hour = true,
                )

            TimePickerDialog(
                onDismissRequest = { showTimePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        val localTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                        onEventTimeChange(localTime)
                        showTimePicker = false
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showTimePicker = false },
                    ) {
                        Text("Annuller")
                    }
                },
            ) {
                TimePicker(state = timePickerState)
            }
        }

        AnimatedVisibility(
            visible = !isKeyboardOpen,
            enter =
                slideInVertically(
                    initialOffsetY = { fullHeight -> fullHeight },
                    animationSpec =
                        spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow,
                        ),
                ) +
                    fadeIn(
                        animationSpec = spring(stiffness = Spring.StiffnessLow),
                    ),
            exit =
                slideOutVertically(
                    targetOffsetY = { fullHeight -> fullHeight },
                    animationSpec =
                        spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMedium,
                        ),
                ) + fadeOut(),
        ) {
            Button(
                onClick = {
                    focusManager.clearFocus()
                    onCreateEvent()
                },
                enabled = !uiState.isLoading,
                modifier =
                    Modifier
                        .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(
                        text = "Opret event",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = confirmButton,
        dismissButton = dismissButton,
        text = content,
    )
}

@Preview
@Composable
private fun AddEventScreenPreview() {
    FlotMandTheme {
        AddEventScreenContent(
            uiState =
                AddEventUiState(
                    event =
                        Event.create(
                            eventName = "Middag hos Mikkel",
                            location = "Flotmand Alle 4",
                            eventDate = LocalDate.now().plusDays(5),
                            eventStartTime = LocalTime.of(19, 30),
                        ),
                    locationTextFieldValue = TextFieldValue("Flotmand Alle 4"),
                ),
            onEventNameChange = {},
            onLocationChange = {},
            onEventDateChange = {},
            onDescriptionChange = {},
            onEventTimeChange = {},
            onAddressSelected = {},
            onClearPredictions = {},
            onCreateEvent = {},
            isKeyboardOpen = false,
            focusManager = LocalFocusManager.current,
        )
    }
}
