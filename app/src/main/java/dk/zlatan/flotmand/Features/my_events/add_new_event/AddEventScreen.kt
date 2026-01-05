package dk.zlatan.flotmand.Features.my_events.add_new_event

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.layout.size
import dk.zlatan.flotmand.Features.my_events.add_new_event.ui.EventTextField
import dk.zlatan.flotmand.design_system.componenets.spacers.VSpacer
import dk.zlatan.flotmand.design_system.theme.FlotMandTheme
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

    // Navigate back when event is created successfully
    LaunchedEffect(uiState.isEventCreated) {
        if (uiState.isEventCreated) {
            onDismiss()
        }
    }

    AddEventScreenContent(
        modifier = modifier,
        uiState = uiState,
        onEventNameChange = viewModel::onEventNameChange,
        onLocationChange = viewModel::onLocationChange,
        onEventDateChange = viewModel::onEventDateChange,
        onEventTimeChange = viewModel::onEventTimeChange,
        onShowDatePicker = viewModel::showDatePicker,
        onHideDatePicker = viewModel::hideDatePicker,
        onShowTimePicker = viewModel::showTimePicker,
        onHideTimePicker = viewModel::hideTimePicker,
        onCreateEvent = viewModel::createEvent,
        onDismiss = onDismiss,
        onClearError = viewModel::clearError
    )
}

@Composable
private fun AddEventScreenContent(
    modifier: Modifier = Modifier,
    uiState: AddEventUiState = AddEventUiState(),
    onEventNameChange: (String) -> Unit = {},
    onLocationChange: (String) -> Unit = {},
    onEventDateChange: (LocalDate) -> Unit = {},
    onEventTimeChange: (LocalTime) -> Unit = {},
    onShowDatePicker: () -> Unit = {},
    onHideDatePicker: () -> Unit = {},
    onShowTimePicker: () -> Unit = {},
    onHideTimePicker: () -> Unit = {},
    onCreateEvent: () -> Unit = {},
    onDismiss: () -> Unit = {},
    onClearError: () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }

    // Show error message in snackbar
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            onClearError()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header with close button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.onPrimaryContainer)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Opret nyt event",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Luk",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Form content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.Top
        ) {
            VSpacer(20.dp)

            // Event Name
            EventTextField(
                label = "Event navn",
                value = uiState.eventName,
                onValueChange = onEventNameChange,
                placeholder = "Fx. Middag hos Gustav"
            )

            VSpacer(20.dp)

            // Location
            EventTextField(
                label = "Lokation",
                value = uiState.location,
                onValueChange = onLocationChange,
                placeholder = "Fx. flotmand alle 4"
            )

            VSpacer(20.dp)

            // Event Date - Clickable field that opens DatePicker
            EventTextField(
                label = "Dato",
                value = uiState.eventDate?.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) ?: "",
                onValueChange = { }, // Read-only
                placeholder = "Vælg dato",
                onClick = onShowDatePicker
            )

            VSpacer(20.dp)

            // Event Time - Clickable field that opens TimePicker
            EventTextField(
                label = "Tidspunkt",
                value = uiState.eventTime?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "",
                onValueChange = { }, // Read-only
                placeholder = "Vælg tidspunkt",
                onClick = onShowTimePicker
            )

            VSpacer(32.dp)
        }

        // Bottom button
        Button(
            onClick = onCreateEvent,
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

        // Snackbar host for error messages
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.padding(16.dp)
        )
    }

    // DatePicker Dialog
    if (uiState.showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.eventDate?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
        )

        DatePickerDialog(
            onDismissRequest = onHideDatePicker,
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val localDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        onEventDateChange(localDate)
                    }
                    onHideDatePicker()
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = onHideDatePicker) {
                    Text("Annuller")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // TimePicker Dialog
    if (uiState.showTimePicker) {
        @OptIn(ExperimentalMaterial3Api::class)
        val timePickerState = rememberTimePickerState(
            initialHour = uiState.eventTime?.hour ?: 18,
            initialMinute = uiState.eventTime?.minute ?: 0,
            is24Hour = true
        )

        TimePickerDialog(
            onDismissRequest = onHideTimePicker,
            confirmButton = {
                TextButton(onClick = {
                    @OptIn(ExperimentalMaterial3Api::class)
                    val localTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                    onEventTimeChange(localTime)
                    onHideTimePicker()
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = onHideTimePicker) {
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
            onDismiss = {},
            onCreateEvent = {}
        )
    }
}