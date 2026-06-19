package dk.zlatan.flotmand.Features.my_events

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.zlatan.flotmand.model.Event
import dk.zlatan.flotmand.model.EventStatus
import dk.zlatan.flotmand.model.User
import dk.zlatan.flotmand.model.service.AccountService
import dk.zlatan.flotmand.model.service.DinnerEventService
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class MyEventUiState(
    val upcomingEvents: List<Event> = emptyList(),
    val pastEvents: List<Event> = emptyList(),
    val publishers: Map<String, User> = emptyMap(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

@HiltViewModel
class MyEventViewModel
    @Inject
    constructor(
        private val dinnerEventService: DinnerEventService,
        private val accountService: AccountService,
    ) : ViewModel() {
        val uiState: StateFlow<MyEventUiState> =
            dinnerEventService.dinnerEventsByUserId
                .map { events ->
                    Log.d(TAG, "Received ${events.size} user events")

                    // Extract unique publisher IDs
                    val publisherIds = events.mapNotNull { it.publisherId }.distinct()
                    Log.d(TAG, "Fetching ${publisherIds.size} unique publishers")

                    // Fetch all publishers in one batch
                    val publishersList =
                        if (publisherIds.isNotEmpty()) {
                            accountService.getUsersByIds(publisherIds)
                        } else {
                            emptyList()
                        }

                    // Create map of publisherId -> User
                    val publishersMap = publishersList.associateBy { it.id }
                    Log.d(TAG, "Loaded ${publishersMap.size} publishers")

                    val upcomingEvents = events
                        .filter { it.status == EventStatus.UPCOMING || it.status == EventStatus.ONGOING }
                        .sortedBy { it.eventDate }
                    val pastEvents = events
                        .filter { it.status == EventStatus.COMPLETED }
                        .sortedByDescending { it.eventDate }

                    MyEventUiState(
                        upcomingEvents = upcomingEvents,
                        pastEvents = pastEvents,
                        publishers = publishersMap,
                        isLoading = false,
                        errorMessage = null,
                    )
                }.catch { e ->
                    Log.e(TAG, "Error loading user events: ${e.message}", e)
                    emit(
                        MyEventUiState(
                            upcomingEvents = emptyList(),
                            pastEvents = emptyList(),
                            publishers = emptyMap(),
                            isLoading = false,
                            errorMessage = "Kunne ikke hente dine events. Prøv igen senere.",
                        ),
                    )
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(500),
                    initialValue = MyEventUiState(isLoading = true),
                )

        fun deleteEvents(eventIds: Set<String>) {
            viewModelScope.launch {
                eventIds.forEach { id ->
                    try { dinnerEventService.deleteDinnerEvent(id) } catch (_: Exception) {}
                }
            }
        }

        companion object {
            private const val TAG = "MyEventViewModel"
        }
    }
