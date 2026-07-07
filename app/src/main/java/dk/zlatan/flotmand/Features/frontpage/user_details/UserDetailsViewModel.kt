package dk.zlatan.flotmand.Features.frontpage.user_details

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.zlatan.flotmand.model.Event
import dk.zlatan.flotmand.model.EventStatus
import dk.zlatan.flotmand.model.User
import dk.zlatan.flotmand.model.service.AccountService
import dk.zlatan.flotmand.model.service.DinnerEventService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class UserDetailsUiState(
    val user: User? = null,
    val isLoading: Boolean = true,
    val isOwnProfile: Boolean = false,
    val eventsHosted: Int = 0,
    val eventsAttended: Int = 0,
    val previousEvents: List<Event> = emptyList(),
)

@HiltViewModel(assistedFactory = UserDetailsViewModel.Factory::class)
class UserDetailsViewModel
    @AssistedInject
    constructor(
        @Assisted private val userId: String,
        private val accountService: AccountService,
        dinnerEventService: DinnerEventService,
    ) : ViewModel() {
        @AssistedFactory
        interface Factory {
            fun create(userId: String): UserDetailsViewModel
        }

        private val _isSavingBio = MutableStateFlow(false)
        val isSavingBio: StateFlow<Boolean> = _isSavingBio.asStateFlow()

        val uiState: StateFlow<UserDetailsUiState> =
            combine(
                accountService.observeUserById(userId),
                dinnerEventService.allDinnerEvents,
            ) { user, allEvents ->
                val completedEvents =
                    allEvents.filter { event ->
                        event.status == EventStatus.COMPLETED &&
                            (event.publisherId == userId || event.participantIds?.contains(userId) == true)
                    }
                val hostedEvents = completedEvents.filter { it.publisherId == userId }

                UserDetailsUiState(
                    user = user,
                    isLoading = false,
                    isOwnProfile = userId == accountService.currentUserId,
                    eventsHosted = hostedEvents.size,
                    eventsAttended = completedEvents.size - hostedEvents.size,
                    previousEvents = hostedEvents.sortedByDescending { it.eventDate },
                )
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserDetailsUiState())

        fun saveBio(newBio: String) {
            viewModelScope.launch {
                _isSavingBio.value = true
                try {
                    accountService.updateBio(newBio)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to save bio: ${e.message}", e)
                } finally {
                    _isSavingBio.value = false
                }
            }
        }

        companion object {
            private const val TAG = "UserDetailsViewModel"
        }
    }