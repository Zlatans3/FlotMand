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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Today
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dk.zlatan.flotmand.Features.my_events.add_new_event.ui.AddressAutocompleteDropdown
import dk.zlatan.flotmand.Features.my_events.add_new_event.ui.EventTextField
import dk.zlatan.flotmand.Features.my_events.add_new_event.ui.openPexelsSearch
import dk.zlatan.flotmand.Features.my_events.edit_event.EditEventViewModel
import dk.zlatan.flotmand.R
import dk.zlatan.flotmand.design_system.componenets.spacers.VSpacer
import dk.zlatan.flotmand.design_system.icon.FmIcons
import dk.zlatan.flotmand.model.AddressPrediction
import dk.zlatan.flotmand.model.Event
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddEventScreen(
    modifier: Modifier = Modifier,
    votingId: String? = null,
    viewModel: AddEventViewModel =
        hiltViewModel<AddEventViewModel, AddEventViewModel.Factory>(
            key = votingId,
            creationCallback = { factory ->
                factory.create(votingId)
            },
        ),
    onDismiss: () -> Unit = {},
    onEventCreated: (eventId: String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val density = LocalDensity.current
    val imeHeight = WindowInsets.ime.getBottom(density)
    val isKeyboardOpen = imeHeight > 0
    val focusManager = LocalFocusManager.current

    // Navigate to event detail when event is created, then reset state.
    // Delay gives the confetti overlay time to play before the screen transitions.
    LaunchedEffect(uiState.isEventCreated) {
        uiState.isEventCreated?.let { eventId ->
            delay(1_500)
            onEventCreated(eventId)
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

    val confettiComposition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.confetti),
    )

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    var tapCount by remember { mutableStateOf(0) }
                    var lastTapMs by remember { mutableStateOf(0L) }
                    Text(
                        text = stringResource(R.string.create_new_event_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                        ) {
                            val now = System.currentTimeMillis()
                            if (now - lastTapMs > 2000L) tapCount = 0
                            lastTapMs = now
                            if (++tapCount >= 5) {
                                tapCount = 0
                                viewModel.autofillTestData()
                            }
                        },
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.close),
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
                                    text = stringResource(R.string.create),
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
        EventScreenContent(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(
                        start = paddingValues.calculateLeftPadding(androidx.compose.ui.unit.LayoutDirection.Ltr),
                        top = paddingValues.calculateTopPadding(),
                        end = paddingValues.calculateRightPadding(androidx.compose.ui.unit.LayoutDirection.Rtl),
                        bottom = 0.dp,
                    ),
            event = uiState.event,
            isLoading = uiState.isLoading,
            addressPredictions = uiState.addressPredictions,
            isLoadingPredictions = uiState.isLoadingPredictions,
            locationTextFieldValue = uiState.locationTextFieldValue,
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
            isEditMode = false,
            onEventImageUrlChange = viewModel::onEventImageUrlChange,
            onImageUrlFromClipboard = viewModel::onImageUrlFromClipboard,
            onImageFocusChange = viewModel::onImageFocusChange,
        )
        }

        if (uiState.isEventCreated != null) {
            LottieAnimation(
                composition = confettiComposition,
                iterations = 1,
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(1f),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EditEventScreen(
    modifier: Modifier = Modifier,
    eventId: String,
    viewModel: EditEventViewModel =
        hiltViewModel<EditEventViewModel, EditEventViewModel.Factory>(
            key = eventId,
            creationCallback = { factory ->
                factory.create(eventId)
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

    LaunchedEffect(uiState.isEventUpdated) {
        if (uiState.isEventUpdated) {
            onDismiss()
//            viewModel.resetState()
        }
    }
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
                        text = stringResource(R.string.edit_event_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.close),
                        )
                    }
                },
                actions = {
                    // Show "Gem" button in header when keyboard is open
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
                                viewModel.updateEvent()
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
                                    text = stringResource(R.string.update),
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
        EventScreenContent(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = paddingValues.calculateLeftPadding(androidx.compose.ui.unit.LayoutDirection.Ltr),
                    top = paddingValues.calculateTopPadding(),
                    end = paddingValues.calculateRightPadding(androidx.compose.ui.unit.LayoutDirection.Rtl),
                    bottom = 0.dp,
                ),
            event = uiState.event,
            isLoading = uiState.isLoading,
            addressPredictions = uiState.addressPredictions,
            isLoadingPredictions = uiState.isLoadingPredictions,
            locationTextFieldValue = uiState.locationTextFieldValue,
            onEventNameChange = viewModel::onEventNameChange,
            onLocationChange = viewModel::onLocationChange,
            onEventDateChange = viewModel::onEventDateChange,
            onDescriptionChange = viewModel::onDescriptionChange,
            onEventTimeChange = viewModel::onEventTimeChange,
            onAddressSelected = viewModel::onAddressSelected,
            onClearPredictions = viewModel::clearAddressPredictions,
            onCreateEvent = viewModel::updateEvent,
            isKeyboardOpen = isKeyboardOpen,
            focusManager = focusManager,
            isEditMode = true,
            onEventImageUrlChange = viewModel::onEventImageUrlChange,
            onImageUrlFromClipboard = viewModel::onImageUrlFromClipboard,
            onImageFocusChange = viewModel::onImageFocusChange,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventScreenContent(
    modifier: Modifier = Modifier,
    event: Event,
    isLoading: Boolean,
    addressPredictions: List<AddressPrediction>,
    isLoadingPredictions: Boolean,
    locationTextFieldValue: TextFieldValue,
    onEventNameChange: (String) -> Unit,
    onLocationChange: (TextFieldValue) -> Unit,
    onEventDateChange: (LocalDate) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onEventTimeChange: (LocalTime) -> Unit,
    onAddressSelected: (AddressPrediction) -> Unit,
    onClearPredictions: () -> Unit,
    focusManager: FocusManager,
    onCreateEvent: () -> Unit,
    isEditMode: Boolean,
    isKeyboardOpen: Boolean,
    onEventImageUrlChange: (String) -> Unit = {},
    onImageUrlFromClipboard: (String) -> Unit = {},
    onImageFocusChange: (Float) -> Unit = {},
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showImageInfoSheet by remember { mutableStateOf(false) }
    var imageLoadError by remember { mutableStateOf(false) }
    var awaitingPexelsClipboard by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val locationFieldRequester = remember { BringIntoViewRequester() }
    val imageInfoSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(event.eventImageUrl) { imageLoadError = false }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, lifecycleEvent ->
            if (lifecycleEvent == Lifecycle.Event.ON_RESUME && awaitingPexelsClipboard) {
                awaitingPexelsClipboard = false
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val text = clipboard.primaryClip?.getItemAt(0)?.text?.toString()
                if (text != null) onImageUrlFromClipboard(text)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(addressPredictions.isNotEmpty(), locationTextFieldValue.text) {
        if (addressPredictions.isNotEmpty() || locationTextFieldValue.text.isNotEmpty()) {
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
            label = stringResource(R.string.event_name_label),
            value = event.eventName.orEmpty(),
            onValueChange = onEventNameChange,
            placeholder = stringResource(R.string.event_name_placeholder),
            singleLine = true,
            maxChar = 100,
            maxLines = 1,
        )

        VSpacer(16.dp)

        // Description (optional) directly under name, larger multi-line
        EventTextField(
            label = stringResource(R.string.description_label),
            value = event.description.orEmpty(),
            onValueChange = { desc -> onDescriptionChange(desc) },
            placeholder = stringResource(R.string.description_placeholder),
            singleLine = false,
            maxChar = 2000,
            minLines = 3,
            maxLines = 6,
        )

        VSpacer(16.dp)

        // Image URL field
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.image_url_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                IconButton(
                    onClick = { showImageInfoSheet = true },
                    modifier = Modifier.size(32.dp).padding(start = 4.dp),
                ) {
                    Icon(
                        imageVector = FmIcons.info,
                        contentDescription = stringResource(R.string.image_url_info_content_description),
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            VSpacer(8.dp)
            OutlinedTextField(
                value = event.eventImageUrl.orEmpty(),
                onValueChange = {
                    onEventImageUrlChange(it)
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = stringResource(R.string.image_url_placeholder),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
                trailingIcon = {
                    IconButton(onClick = {
                        focusManager.clearFocus()
                        awaitingPexelsClipboard = true
                        openPexelsSearch(context, event.eventName.orEmpty())
                    }) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = stringResource(R.string.open_pexels_content_description),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                ),
            )

            val imageUrl = event.eventImageUrl
            if (!imageUrl.isNullOrBlank()) {
                VSpacer(8.dp)
                val focusY = (event.imageFocusY ?: 0.5).toFloat()
                // pointerInput is keyed on the url, so the drag lambda would capture a
                // stale focus value without rememberUpdatedState.
                val currentFocusY by rememberUpdatedState(focusY)
                var previewHeightPx by remember { mutableIntStateOf(0) }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .onSizeChanged { previewHeightPx = it.height }
                        .pointerInput(imageUrl) {
                            detectVerticalDragGestures { change, dragAmount ->
                                change.consume()
                                if (previewHeightPx > 0) {
                                    // Dragging down reveals content further up the image.
                                    val delta = -dragAmount / (previewHeightPx * 2f)
                                    onImageFocusChange((currentFocusY + delta).coerceIn(0f, 1f))
                                }
                            }
                        },
                ) {
                    val context = LocalContext.current
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        alignment = BiasAlignment(0f, focusY * 2f - 1f),
                        modifier = Modifier.fillMaxSize(),
                        onError = { imageLoadError = true },
                        onSuccess = { imageLoadError = false },
                    )
                    if (imageLoadError) {
                        Text(
                            text = stringResource(R.string.image_load_error),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(horizontal = 16.dp),
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.event_image_drag_hint),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 8.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color.Black.copy(alpha = 0.45f))
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                        )
                    }
                }
            }
        }

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
                    label = stringResource(R.string.location_label),
                    value = locationTextFieldValue,
                    onValueChange = onLocationChange,
                    placeholder = stringResource(R.string.location_placeholder),
                    trailingIcon = {
                        IconButton(onClick = { /* optional: open map picker in future */ }) {
                            Icon(imageVector = Icons.Filled.Place, contentDescription = stringResource(R.string.location_label))
                        }
                    },
                )

                // Autocomplete dropdown
                AddressAutocompleteDropdown(
                    predictions = addressPredictions,
                    isLoading = isLoadingPredictions,
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
                label = stringResource(R.string.date_label),
                value = event.eventDate?.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")).orEmpty(),
                onValueChange = { }, // Read-only
                placeholder = stringResource(R.string.date_placeholder),
                modifier = Modifier.weight(1f),
                onClick = {
                    focusManager.clearFocus()
                    showDatePicker = true
                },
                maxLines = 1,
                singleLine = true,
                trailingIcon = {
                    IconButton(onClick = {
                        focusManager.clearFocus()
                        showDatePicker = true
                    }) {
                        Icon(imageVector = Icons.Filled.Today, contentDescription = stringResource(R.string.select_date))
                    }
                },
            )

            Spacer(modifier = Modifier.size(12.dp))

            EventTextField(
                label = stringResource(R.string.time_label),
                value = event.eventStartTime?.format(DateTimeFormatter.ofPattern("HH:mm")).orEmpty(),
                onValueChange = { }, // Read-only
                placeholder = stringResource(R.string.time_placeholder),
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
                            contentDescription = stringResource(R.string.select_time),
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
                        event.eventDate
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

        // TimePicker Dialog
        if (showTimePicker) {
            val timePickerState =
                rememberTimePickerState(
                    initialHour = event.eventStartTime?.hour ?: 18,
                    initialMinute = event.eventStartTime?.minute ?: 0,
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
                        Text(stringResource(R.string.ok))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showTimePicker = false },
                    ) {
                        Text(stringResource(R.string.cancel))
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
                enabled = !isLoading,
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
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(
                        text = if (isEditMode) stringResource(R.string.update) else stringResource(R.string.create_event),
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                }
            }
        }
    } // Column

    if (showImageInfoSheet) {
        ModalBottomSheet(
            onDismissRequest = { showImageInfoSheet = false },
            sheetState = imageInfoSheetState,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = stringResource(R.string.image_url_info_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                val steps = listOf(
                    stringResource(R.string.image_url_info_step_1),
                    stringResource(R.string.image_url_info_step_2),
                    stringResource(R.string.image_url_info_step_3),
                    stringResource(R.string.image_url_info_step_4),
                    stringResource(R.string.image_url_info_step_5),
                )
                steps.forEachIndexed { index, step ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Text(
                            text = "${index + 1}.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = step,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
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
    MaterialTheme {
        AddEventScreen(
            onEventCreated = {},
        )
    }
}

@Preview(showBackground = true, name = "Edit Event Preview")
@Composable
private fun EditEventScreenPreview() {
    MaterialTheme {
        EditEventScreen(eventId = "previewEventId")
    }
}
