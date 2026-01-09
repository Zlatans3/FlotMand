package dk.zlatan.flotmand.Features.frontpage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.zlatan.flotmand.model.Event
import dk.zlatan.flotmand.model.User
import dk.zlatan.flotmand.model.service.AccountService
import dk.zlatan.flotmand.model.service.DinnerEventService
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class FrontPageUiState(
    val eventList: List<Event> = emptyList(),
    val publishers: Map<String, User> = emptyMap(),
    val currentUser: User,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

@HiltViewModel
class FrontPageViewModel @Inject constructor(
    dinnerEventService: DinnerEventService,
    private val accountService: AccountService
) : ViewModel() {

    // TODO: Zlatan 09/01/2026 Putting filternotnull could hide errors when currentUser is null
    val uiState: StateFlow<FrontPageUiState> = combine(
        dinnerEventService.allDinnerEvents,
        accountService.currentUser.filterNotNull()
    ) { events, currentUser ->
        val sortedEvents = events.sortedByDescending { it.eventDate }
        val publisherIds = sortedEvents.mapNotNull { it.publisherId }.distinct()

        val publishersMap = if (publisherIds.isNotEmpty()) {
            try {
                viewModelScope.async { fetchPublishersMap(publisherIds) }.await()
            } catch (_: Exception) {
                emptyMap()
            }
        } else {
            emptyMap()
        }

        FrontPageUiState(
            eventList = sortedEvents,
            publishers = publishersMap,
            currentUser = currentUser,
            isLoading = false,
            errorMessage = null
        )
    }
        .catch { _ ->
            emit(
                FrontPageUiState(
                    eventList = emptyList(),
                    publishers = emptyMap<String, User>(),
                    currentUser = User(),
                    isLoading = false,
                    errorMessage = "Kunne ikke hente events. Prøv igen senere."
                )
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = FrontPageUiState(
                currentUser = User(),
                isLoading = true
            )
        )

    private suspend fun fetchPublishersMap(publisherIds: List<String>): Map<String, User> {
        return try {
            accountService.getUsersByIds(publisherIds).associateBy { it.id }
        } catch (_: Exception) {
            emptyMap()
        }
    }
}