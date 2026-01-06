package dk.zlatan.flotmand.Features.frontpage

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.zlatan.flotmand.model.Event
import dk.zlatan.flotmand.model.User
import dk.zlatan.flotmand.model.service.AccountService
import dk.zlatan.flotmand.model.service.DinnerEventService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class FrontPageUiState(
    val eventList: List<Event> = emptyList(),
    val publishers: Map<String, User> = emptyMap(), // Map of publisherId -> User
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

@HiltViewModel
class FrontPageViewModel @Inject constructor(
    dinnerEventService: DinnerEventService,
    private val accountService: AccountService
) : ViewModel() {

    val uiState: StateFlow<FrontPageUiState> = dinnerEventService.allDinnerEvents
        .map { events ->
            Log.d(TAG, "Received ${events.size} events")

            // Extract unique publisher IDs
            val publisherIds = events.mapNotNull { it.publisherId }.distinct()
            Log.d(TAG, "Fetching ${publisherIds.size} unique publishers")

            // Fetch all publishers in one batch
            val publishersList = if (publisherIds.isNotEmpty()) {
                accountService.getUsersByIds(publisherIds)
            } else {
                emptyList()
            }

            // Create map of publisherId -> User
            val publishersMap = publishersList.associateBy { it.id }
            Log.d(TAG, "Loaded ${publishersMap.size} publishers")

            events.forEach { event ->
                val publisherName = publishersMap[event.publisherId]?.displayName ?: "Unknown"
                Log.d(TAG, "Event: ${event.eventName}, Publisher: $publisherName")
            }

            FrontPageUiState(
                eventList = events.sortedByDescending { it.eventDate },
                publishers = publishersMap,
                isLoading = false,
                errorMessage = null
            )
        }
        .catch { e ->
            Log.e(TAG, "Error loading events: ${e.message}", e)
            emit(
                FrontPageUiState(
                    eventList = emptyList(),
                    publishers = emptyMap(),
                    isLoading = false,
                    errorMessage = "Kunne ikke hente events. Prøv igen senere."
                )
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = FrontPageUiState(isLoading = true)
        )

    companion object {
        private const val TAG = "FrontPageViewModel"
    }
}