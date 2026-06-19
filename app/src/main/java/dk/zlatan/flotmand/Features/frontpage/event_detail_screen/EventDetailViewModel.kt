package dk.zlatan.flotmand.Features.frontpage.event_detail_screen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.zlatan.flotmand.model.Event
import dk.zlatan.flotmand.model.User
import dk.zlatan.flotmand.model.service.AccountService
import dk.zlatan.flotmand.model.service.DinnerEventService
import dk.zlatan.flotmand.model.service.NotificationService
import dk.zlatan.flotmand.util.PhoneDialogRepository
import dk.zlatan.flotmand.util.combine
import dk.zlatan.flotmand.util.feature_flags.FeatureKey
import dk.zlatan.flotmand.util.feature_flags.FeatureFlagManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class RsvpStatus { NONE, LOADING, ACCEPTED, DECLINED }

/**
 * UI state for Event Detail screen.
 * - rsvpStatus LOADING means a toggle is in-flight.
 */
data class EventDetailUiState(
    val event: Event? = null,
    val publisher: User? = null,
    val participants: List<User> = emptyList(),
    val declinedUsers: List<User> = emptyList(),
    val isLoadingEvent: Boolean = false,
    val showParticipationBottomSheet: Boolean = false,
    val isPublisher: Boolean = false,
    val rsvpStatus: RsvpStatus = RsvpStatus.NONE,
    val isDeleted: Boolean = false,
    val eventError: String? = null,
    /** Raw text the host has typed into the total-price field. */
    val totalPriceInput: String = "",
    /** Total price divided by participant count; null when input is blank or unparseable. */
    val pricePerPerson: Double? = null,
    val isSavingPrice: Boolean = false,
    val priceError: String? = null,
) {
    val isParticipated: Boolean? get() = when (rsvpStatus) {
        RsvpStatus.LOADING -> null
        RsvpStatus.ACCEPTED -> true
        else -> false
    }
}

/** Bundles price-input UI state into a single flow to stay within the 10-flow combine limit. */
private data class PriceInputState(
    val input: String = "",
    val isSaving: Boolean = false,
    val error: String? = null,
)

/** Bundles both user lists into a single flow to stay within the 10-flow combine limit. */
private data class ParticipantsData(
    val joined: List<User> = emptyList(),
    val declined: List<User> = emptyList(),
)

@HiltViewModel(assistedFactory = EventDetailViewModel.Factory::class)
internal class EventDetailViewModel
    @AssistedInject
    constructor(
        @Assisted private val eventId: String,
        private val dinnerEventService: DinnerEventService,
        private val accountService: AccountService,
        private val notificationService: NotificationService,
        private val featureFlagManager: FeatureFlagManager,
        private val phoneDialogRepository: PhoneDialogRepository,
    ) : ViewModel() {
        @AssistedFactory
        interface Factory {
            fun create(eventId: String): EventDetailViewModel
        }

        val shouldPromptPhone: StateFlow<Boolean> =
            combine(
                featureFlagManager.isEnabled(FeatureKey.SHOW_PHONE_DIALOG_ON_PRICE_ADDED),
                phoneDialogRepository.isDismissed,
            ) { featureFlag, dismissed -> featureFlag && !dismissed }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

        fun onPhoneDialogDismissed() {
            viewModelScope.launch { phoneDialogRepository.dismiss() }
        }

        // Internal state flows
        private val _event = MutableStateFlow<Event?>(null)
        private val _publisher = MutableStateFlow<User?>(null)
        private val _participantsData = MutableStateFlow(ParticipantsData())
        private val _isLoadingEvent = MutableStateFlow(false)
        private val _showParticipationBottomSheet = MutableStateFlow(false)
        private val _isPublisher = MutableStateFlow(false)
        private val _rsvpStatus = MutableStateFlow(RsvpStatus.NONE)
        private val _isDeleted = MutableStateFlow(false)
        private val _eventError = MutableStateFlow<String?>(null)
        private val _priceInputState = MutableStateFlow(PriceInputState())

        // Guards against overwriting the host's in-progress edit when Firestore re-emits.
        private var totalPriceInitialized = false

        private var eventObserverJob: Job? = null

        val uiState: StateFlow<EventDetailUiState> =
            combine(
                _event,
                _publisher,
                _participantsData,
                _isLoadingEvent,
                _showParticipationBottomSheet,
                _isPublisher,
                _rsvpStatus,
                _isDeleted,
                _eventError,
                _priceInputState,
            ) {
                event,
                publisher,
                participantsData,
                isLoadingEvent,
                showParticipationBottomSheet,
                isPublisher,
                rsvpStatus,
                isDeleted,
                eventError,
                priceInputState,
                ->
                val pricePerPerson = priceInputState.input.toDoubleOrNull()
                    ?.let { total ->
                        val count = participantsData.joined.size
                        if (count > 0) total / count else null
                    }
                EventDetailUiState(
                    event = event,
                    publisher = publisher,
                    participants = participantsData.joined,
                    declinedUsers = participantsData.declined,
                    isLoadingEvent = isLoadingEvent,
                    showParticipationBottomSheet = showParticipationBottomSheet,
                    isPublisher = isPublisher,
                    rsvpStatus = rsvpStatus,
                    isDeleted = isDeleted,
                    eventError = eventError,
                    totalPriceInput = priceInputState.input,
                    pricePerPerson = pricePerPerson,
                    isSavingPrice = priceInputState.isSaving,
                    priceError = priceInputState.error,
                )
            }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                EventDetailUiState(),
            )

        init {
            Log.d(TAG, "ViewModel init: eventId=$eventId")
            observeEvent()
            notificationService.markAsReadByReferenceId(eventId)
        }

        private fun observeEvent() {
            viewModelScope.launch {
                Log.e(TAG, "Starting to observe event: id=$eventId")
                _isLoadingEvent.update { true }
                try {
                    dinnerEventService
                        .observeDinnerEvent(eventId)
                        .catch {
                            Log.e(TAG, "catched zlatanb ${it.message}", it)
                            _eventError.value = it.message
                            _isLoadingEvent.value = false
                        }.collectLatest { event ->
                            if (event == null) {
                                Log.w(TAG, "Event not found or deleted: id=$eventId")
                                onEventUnavailable()
                                _isLoadingEvent.value = false
                                return@collectLatest
                            }

                            val currentUserId = accountService.currentUserId
                            val isPublisher =
                                event.publisherId != null && event.publisherId == currentUserId

                            if (_rsvpStatus.value != RsvpStatus.LOADING) {
                                val rsvp = when {
                                    event.participantIds?.contains(currentUserId) == true -> RsvpStatus.ACCEPTED
                                    event.declinedIds?.contains(currentUserId) == true -> RsvpStatus.DECLINED
                                    else -> RsvpStatus.NONE
                                }
                                _rsvpStatus.value = rsvp
                            }

                            if (!totalPriceInitialized) {
                                _priceInputState.update {
                                    it.copy(
                                        input = event.totalPrice
                                            ?.toBigDecimal()?.stripTrailingZeros()?.toPlainString().orEmpty(),
                                    )
                                }
                                totalPriceInitialized = true
                            }

                            _event.value = event
                            _isPublisher.value = isPublisher
                            _isLoadingEvent.value = false

                            _publisher.value = loadPublisherSync(event.publisherId)
                            val joined = loadParticipantsSync(event.participantIds)
                            val declined = loadParticipantsSync(event.declinedIds)
                            _participantsData.value = ParticipantsData(joined = joined, declined = declined)
                        }
                } catch (e: Exception) {
                    Log.e(TAG, "Error observing event: ${e.message}", e)
                    _eventError.value = e.message
                    _isLoadingEvent.update { false }
                }
            }
        }

        private suspend fun loadPublisherSync(publisherId: String?): User? {
            if (publisherId.isNullOrBlank()) return null
            return try {
                accountService.getUsersByIds(listOf(publisherId)).firstOrNull()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load publisher: ${e.message}", e)
                null
            }
        }

        private suspend fun loadParticipantsSync(ids: List<String>?): List<User> {
            if (ids.isNullOrEmpty()) return emptyList()
            return try {
                accountService.getUsersByIds(ids)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load users: ${e.message}", e)
                emptyList()
            }
        }

        fun onUserRsvp(accepted: Boolean) {
            viewModelScope.launch {
                val currentEvent = _event.value
                val userId = accountService.currentUserId

                if (currentEvent == null) {
                    Log.w(TAG, "onUserRsvp called but event is null")
                    return@launch
                }
                if (currentEvent.publisherId == userId) {
                    Log.i(TAG, "Publisher cannot RSVP; ignoring. publisherId=$userId")
                    return@launch
                }

                try {
                    _rsvpStatus.value = RsvpStatus.LOADING

                    val currentParticipantIds = currentEvent.participantIds ?: emptyList()
                    val currentDeclinedIds = currentEvent.declinedIds ?: emptyList()

                    val newParticipantIds: List<String>
                    val newDeclinedIds: List<String>

                    if (accepted) {
                        val alreadyAccepted = currentParticipantIds.contains(userId)
                        newParticipantIds = if (alreadyAccepted) {
                            currentParticipantIds.filterNot { it == userId }
                        } else {
                            currentParticipantIds + userId
                        }
                        newDeclinedIds = currentDeclinedIds.filterNot { it == userId }
                        _rsvpStatus.value = if (alreadyAccepted) RsvpStatus.NONE else RsvpStatus.ACCEPTED
                    } else {
                        val alreadyDeclined = currentDeclinedIds.contains(userId)
                        newDeclinedIds = if (alreadyDeclined) {
                            currentDeclinedIds.filterNot { it == userId }
                        } else {
                            currentDeclinedIds + userId
                        }
                        newParticipantIds = currentParticipantIds.filterNot { it == userId }
                        _rsvpStatus.value = if (alreadyDeclined) RsvpStatus.NONE else RsvpStatus.DECLINED
                    }

                    val updatedEvent = currentEvent.copy(
                        participantIds = newParticipantIds,
                        declinedIds = newDeclinedIds,
                    )
                    dinnerEventService.updateDinnerEvent(updatedEvent)
                    _event.value = updatedEvent

                    val joined = loadParticipantsSync(newParticipantIds)
                    val declined = loadParticipantsSync(newDeclinedIds)
                    _participantsData.value = ParticipantsData(joined = joined, declined = declined)
                    _showParticipationBottomSheet.value = false

                    Log.d(TAG, "RSVP updated: accepted=$accepted for event ${updatedEvent.eventId}")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to update RSVP: ${e.message}", e)
                    val fallback = when {
                        currentEvent.participantIds?.contains(userId) == true -> RsvpStatus.ACCEPTED
                        currentEvent.declinedIds?.contains(userId) == true -> RsvpStatus.DECLINED
                        else -> RsvpStatus.NONE
                    }
                    _rsvpStatus.value = fallback
                }
            }
        }

        private fun clearAll() {
            _event.value = null
            _publisher.value = null
            _participantsData.value = ParticipantsData()
            _isLoadingEvent.value = false
            _showParticipationBottomSheet.value = false
            _isPublisher.value = false
            _rsvpStatus.value = RsvpStatus.NONE
            _isDeleted.value = false
            _eventError.value = null
            _priceInputState.value = PriceInputState()
            totalPriceInitialized = false
        }

        fun onEventUnavailable() {
            clearAll()
        }

        fun onDismissParticipantsSheet() {
            _showParticipationBottomSheet.value = false
        }

        fun showParticipants() {
            _showParticipationBottomSheet.value = true
        }

        fun deleteEvent(eventId: String) {
            viewModelScope.launch {
                try {
                    dinnerEventService.deleteDinnerEvent(eventId)
                    clearAll()
                    _isDeleted.value = true
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to delete event: ${e.message}", e)
                }
            }
        }

        fun onTotalPriceChanged(input: String) {
            _priceInputState.update { it.copy(input = input, error = null) }
        }

        fun saveTotalPrice() {
            viewModelScope.launch {
                if (!_isPublisher.value) return@launch
                if (_priceInputState.value.isSaving) return@launch
                val currentEvent = _event.value ?: return@launch
                val price = _priceInputState.value.input.toDoubleOrNull() ?: return@launch

                _priceInputState.update { it.copy(isSaving = true, error = null) }
                try {
                    val updatedEvent = currentEvent.copy(totalPrice = price)
                    dinnerEventService.updateDinnerEvent(updatedEvent)
                    _event.value = updatedEvent
                    _priceInputState.update { it.copy(isSaving = false) }
                    Log.d(TAG, "saveTotalPrice: saved $price for event ${currentEvent.eventId}")
                } catch (e: Exception) {
                    Log.e(TAG, "saveTotalPrice: failed — ${e.message}", e)
                    _priceInputState.update { it.copy(isSaving = false, error = "Kunne ikke gemme prisen. Prøv igen.") }
                }
            }
        }

        companion object {
            private const val TAG = "EventDetailViewModel"
        }
    }
