package dk.zlatan.flotmand.Features.my_events.add_new_event

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.layout.size
import dk.zlatan.flotmand.Features.my_events.add_new_event.ui.EventTextField
import dk.zlatan.flotmand.design_system.componenets.spacers.VSpacer
import dk.zlatan.flotmand.design_system.theme.FlotMandTheme
import dk.zlatan.flotmand.model.Event
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddEventScreenRoute(
    modifier: Modifier = Modifier,
    viewModel: AddEventViewModel = hiltViewModel(),
    onDismiss: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Navigate back when event is created successfully
    LaunchedEffect(uiState.isEventCreated) {
        if (uiState.isEventCreated) {
            onDismiss()
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
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Luk"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Button(
                onClick = viewModel::createEvent,
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Opret event",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    ) { paddingValues ->
        AddEventScreenContent(
            modifier = Modifier.padding(paddingValues),
            uiState = uiState,
            onEventNameChange = viewModel::onEventNameChange,
            onLocationChange = viewModel::onLocationChange,
            onEventDateChange = viewModel::onEventDateChange,
            onEventTimeChange = viewModel::onEventTimeChange,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEventScreenContent(
    modifier: Modifier = Modifier,
    uiState: AddEventUiState,
    onEventNameChange: (String) -> Unit,
    onLocationChange: (String) -> Unit,
    onEventDateChange: (LocalDate) -> Unit,
    onEventTimeChange: (LocalTime) -> Unit,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.Top
    ) {
        VSpacer(20.dp)

        // Event Name
        EventTextField(
            label = "Event navn",
            value = uiState.event.eventName.orEmpty(),
            onValueChange = onEventNameChange,
            placeholder = "Fx. Middag hos Gustav"
        )

        VSpacer(20.dp)

        // Location
        EventTextField(
            label = "Lokation",
            value = uiState.event.location.orEmpty(),
            onValueChange = onLocationChange,
            placeholder = "Fx. flotmand alle 4"
        )

        VSpacer(20.dp)

        // Event Date - Clickable field that opens DatePicker
        EventTextField(
            label = "Dato",
            value = uiState.event.eventDate?.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) ?: "",
            onValueChange = { }, // Read-only
            placeholder = "Vælg dato",
            onClick = { showDatePicker = true }
        )

        VSpacer(20.dp)

        // Event Time - Clickable field that opens TimePicker
        EventTextField(
            label = "Tidspunkt",
            value = uiState.event.eventStartTime?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "",
            onValueChange = { }, // Read-only
            placeholder = "Vælg tidspunkt",
            onClick = { showTimePicker = true }
        )

        VSpacer(32.dp)
    }

    // DatePicker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.event.eventDate?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val localDate = Instant.ofEpochMilli(millis)
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
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // TimePicker Dialog
    if (showTimePicker) {
        @OptIn(ExperimentalMaterial3Api::class)
        val timePickerState = rememberTimePickerState(
            initialHour = uiState.event.eventStartTime?.hour ?: 18,
            initialMinute = uiState.event.eventStartTime?.minute ?: 0,
            is24Hour = true
        )

        TimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    @OptIn(ExperimentalMaterial3Api::class)
                    val localTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                    onEventTimeChange(localTime)
                    showTimePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Annuller")
                }
            }
        ) {
            @OptIn(ExperimentalMaterial3Api::class)
            TimePicker(state = timePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = confirmButton,
        dismissButton = dismissButton,
        text = content
    )
}

@Preview
@Composable
private fun AddEventScreenPreview() {
    FlotMandTheme {
        AddEventScreenContent(
            modifier = Modifier,
            uiState = AddEventUiState(
                event = Event.create(
                    eventName = "Fødselsdagsfest hos Zlatan",
                    location = "Zlatans hus, Flotmand Alle 4",
                    eventDate = LocalDate.now().plusDays(5),
                    eventStartTime = LocalTime.of(19, 30)
                )
            ),
            onEventNameChange = {},
            onLocationChange = {},
            onEventDateChange = {},
            onEventTimeChange = {}
        )
    }
}