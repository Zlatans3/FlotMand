package dk.zlatan.flotmand.Features.frontpage

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
    val publishers: Map<String, User> = emptyMap(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

@HiltViewModel
class FrontPageViewModel @Inject constructor(
    dinnerEventService: DinnerEventService,
    private val accountService: AccountService
) : ViewModel() {

    // TODO: Zlatan 06/01/2026 SHOULD MAYBE BE COMBINED FLOWS
    val uiState: StateFlow<FrontPageUiState> = dinnerEventService.allDinnerEvents
        .map { events ->
            val sortedEvents = events.sortedByDescending { it.eventDate }
            val publisherIds = sortedEvents.mapNotNull { it.publisherId }.distinct()

            val publishersMap = if (publisherIds.isNotEmpty())
                fetchPublishersMap(publisherIds)
            else
                emptyMap()

            FrontPageUiState(
                eventList = sortedEvents,
                publishers = publishersMap,
                isLoading = false,
                errorMessage = null
            )
        }
        .catch { _ ->
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

    private suspend fun fetchPublishersMap(publisherIds: List<String>): Map<String, User> {
        return try {
            accountService.getUsersByIds(publisherIds).associateBy { it.id }
        } catch (_: Exception) {
            emptyMap()
        }
    }

    companion object {
        private const val TAG = "FrontPageViewModel"
    }
}