package dk.zlatan.flotmand.Features.frontpage.event_detail_screen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.zlatan.flotmand.model.AppNotification
import dk.zlatan.flotmand.model.Event
import dk.zlatan.flotmand.model.NotificationType
import dk.zlatan.flotmand.model.User
import dk.zlatan.flotmand.model.service.AccountService
import dk.zlatan.flotmand.model.service.DinnerEventService
import dk.zlatan.flotmand.model.service.NotificationService
import dk.zlatan.flotmand.util.combine
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI state for Event Detail screen.
 * - isParticipated tri-state: false = not participating (default), null = loading, true = participating
 */
data class EventDetailUiState(
    val event: Event? = null,
    val publisher: User? = null,
    val participants: List<User> = emptyList(),
    val isLoadingEvent: Boolean = false,
    val showParticipationBottomSheet: Boolean = false,
    val isPublisher: Boolean = false,
    val isParticipated: Boolean? = false,
    val isDeleted: Boolean = false,
    val eventError: String? = null,
    /** Raw text the host has typed into the total-price field. */
    val totalPriceInput: String = "",
    /** Total price divided by participant count; null when input is blank or unparseable. */
    val pricePerPerson: Double? = null,
)

@HiltViewModel(assistedFactory = EventDetailViewModel.Factory::class)
internal class EventDetailViewModel
    @AssistedInject
    constructor(
        @Assisted private val eventId: String,
        private val dinnerEventService: DinnerEventService,
        private val accountService: AccountService,
        private val notificationService: NotificationService,
    ) : ViewModel() {
        @AssistedFactory
        interface Factory {
            fun create(eventId: String): EventDetailViewModel
        }

        // Internal state flows
        private val _event = MutableStateFlow<Event?>(null)
        private val _publisher = MutableStateFlow<User?>(null)
        private val _participants = MutableStateFlow<List<User>>(emptyList())
        private val _isLoadingEvent = MutableStateFlow(false)
        private val _showParticipationBottomSheet = MutableStateFlow(false)
        private val _isPublisher = MutableStateFlow(false)
        private val _isParticipated = MutableStateFlow<Boolean?>(false)
        private val _isDeleted = MutableStateFlow(false)
        private val _eventError = MutableStateFlow<String?>(null)
        private val _totalPriceInput = MutableStateFlow("")

        // Guards against overwriting the host's in-progress edit when Firestore re-emits.
        private var totalPriceInitialized = false

        private var eventObserverJob: Job? = null

        val uiState: StateFlow<EventDetailUiState> =
            combine(
                _event,
                _publisher,
                _participants,
                _isLoadingEvent,
                _showParticipationBottomSheet,
                _isPublisher,
                _isParticipated,
                _isDeleted,
                _eventError,
                _totalPriceInput,
            ) {
                event,
                publisher,
                participants,
                isLoadingEvent,
                showParticipationBottomSheet,
                isPublisher,
                isParticipated,
                isDeleted,
                eventError,
                totalPriceInput,
                ->
                val pricePerPerson = totalPriceInput.toDoubleOrNull()
                    ?.let { total -> if (participants.isNotEmpty()) total / participants.size else null }
                EventDetailUiState(
                    event = event,
                    publisher = publisher,
                    participants = participants,
                    isLoadingEvent = isLoadingEvent,
                    showParticipationBottomSheet = showParticipationBottomSheet,
                    isPublisher = isPublisher,
                    isParticipated = isParticipated,
                    isDeleted = isDeleted,
                    eventError = eventError,
                    totalPriceInput = totalPriceInput,
                    pricePerPerson = pricePerPerson,
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
                _isLoadingEvent.update {
                    true
                }
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
                            val isParticipated = event.participantIds?.contains(currentUserId) ?: false

                            if (!totalPriceInitialized) {
                                _totalPriceInput.value = event.totalPrice
                                    ?.toBigDecimal()?.stripTrailingZeros()?.toPlainString() ?: ""
                                totalPriceInitialized = true
                            }

                            _event.value = event
                            _isPublisher.value = isPublisher
                            _isParticipated.value = isParticipated
                            _isLoadingEvent.value = false

                            // Refresh publisher and participants when relevant fields change
                            _publisher.value = loadPublisherSync(event.publisherId)
                            _participants.value = loadParticipantsSync(event.participantIds)
                        }
                } catch (e: Exception) {
                    Log.e(TAG, "Error observing event: ${e.message}", e)
                    _eventError.value = e.message
                    _isLoadingEvent.update {
                        false
                    }
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

        private suspend fun loadParticipantsSync(participantIds: List<String>?): List<User> {
            if (participantIds.isNullOrEmpty()) return emptyList()
            return try {
                accountService.getUsersByIds(participantIds)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load participants: ${e.message}", e)
                emptyList()
            }
        }

        fun onUserParticipate() {
            viewModelScope.launch {
                val currentEvent = _event.value
                val userId = accountService.currentUserId

                if (currentEvent == null) {
                    Log.w(TAG, "onUserParticipate called but event is null")
                    return@launch
                }

                if (currentEvent.publisherId == userId) {
                    Log.i(TAG, "Publisher cannot participate; ignoring click. publisherId=$userId")
                    return@launch
                }

                try {
                    _isParticipated.value = null
                    val existingIds = currentEvent.participantIds ?: emptyList()
                    val isCurrentlyParticipating = existingIds.contains(userId)
                    val updatedIds =
                        if (isCurrentlyParticipating) {
                            existingIds.filterNot { it == userId }
                        } else {
                            existingIds + userId
                        }
                    val updatedEvent = currentEvent.copy(participantIds = updatedIds)
                    dinnerEventService.updateDinnerEvent(updatedEvent)
                    _event.value = updatedEvent
                    _participants.value = loadParticipantsSync(updatedIds)
                    _showParticipationBottomSheet.value = false
                    _isParticipated.value = !isCurrentlyParticipating
                    val action = if (isCurrentlyParticipating) "removed" else "added"
                    Log.d(TAG, "User $userId $action to participants for event ${updatedEvent.eventId}")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to toggle participant: ${e.message}", e)
                    _isParticipated.value = currentEvent.participantIds?.contains(userId) ?: false
                }
            }
        }

        private fun clearAll() {
            _event.value = null
            _publisher.value = null
            _participants.value = emptyList()
            _isLoadingEvent.value = false
            _showParticipationBottomSheet.value = false
            _isPublisher.value = false
            _isParticipated.value = false
            _isDeleted.value = false
            _eventError.value = null
            _totalPriceInput.value = ""
            totalPriceInitialized = false
        }

        // Optionally, add a new function to handle event unavailable state
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
            _totalPriceInput.value = input
        }

        fun saveTotalPrice() {
            viewModelScope.launch {
                if (!_isPublisher.value) return@launch
                val currentEvent = _event.value ?: return@launch
                val price = _totalPriceInput.value.toDoubleOrNull() ?: return@launch
                try {
                    val updatedEvent = currentEvent.copy(totalPrice = price)
                    dinnerEventService.updateDinnerEvent(updatedEvent)
                    _event.value = updatedEvent

                    val recipientIds = currentEvent.participantIds
                        ?.filter { it != currentEvent.publisherId }
                        ?: emptyList()
                    if (recipientIds.isNotEmpty()) {
                        val participantCount = _participants.value.size.takeIf { it > 0 } ?: 1
                        val pricePerPerson = price / participantCount
                        val notification = AppNotification(
                            type = NotificationType.EVENT.value,
                            referenceId = currentEvent.eventId.orEmpty(),
                            title = currentEvent.eventName ?: "Middag",
                            body = "Prisen er sat til ${"%.2f".format(pricePerPerson)} kr. per person",
                            isRead = false,
                            createdAtMillis = System.currentTimeMillis(),
                        )
                        notificationService.sendToUsers(recipientIds, notification)
                    }
                    Log.d(TAG, "saveTotalPrice: saved $price for event ${currentEvent.eventId}")
                } catch (e: Exception) {
                    Log.e(TAG, "saveTotalPrice: failed — ${e.message}", e)
                    _eventError.value = e.message
                }
            }
        }

        companion object {
            private const val TAG = "EventDetailViewModel"
        }
    }
