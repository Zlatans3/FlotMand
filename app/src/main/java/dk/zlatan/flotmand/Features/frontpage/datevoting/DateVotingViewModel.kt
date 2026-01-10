package dk.zlatan.flotmand.Features.frontpage.datevoting

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.zlatan.flotmand.model.DateVoting
import dk.zlatan.flotmand.model.service.AccountService
import dk.zlatan.flotmand.model.service.DateVotingService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class DateVotingUiState(
    val dateVoting: DateVoting? = null,
    val currentUserId: String = "",
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

@HiltViewModel
class DateVotingViewModel @Inject constructor(
    private val dateVotingService: DateVotingService,
    private val accountService: AccountService,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val eventId: String = checkNotNull(savedStateHandle["eventId"])

    val uiState: StateFlow<DateVotingUiState> = dateVotingService
        .observeDateVoting(eventId)
        .map { voting ->
            DateVotingUiState(
                dateVoting = voting,
                currentUserId = accountService.currentUserId,
                isLoading = false,
                errorMessage = null
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DateVotingUiState(
                currentUserId = accountService.currentUserId,
                isLoading = true
            )
        )

    fun onVoteForDate(date: LocalDate) {
        viewModelScope.launch {
            try {
                val votingId = uiState.value.dateVoting?.votingId ?: return@launch
                val userId = accountService.currentUserId
                dateVotingService.addVote(votingId, date, userId)
            } catch (e: Exception) {
                // Handle error - could emit to UI state
            }
        }
    }

    fun onRemoveVote(date: LocalDate) {
        viewModelScope.launch {
            try {
                val votingId = uiState.value.dateVoting?.votingId ?: return@launch
                val userId = accountService.currentUserId
                dateVotingService.removeVote(votingId, date, userId)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun onAddDateOption(date: LocalDate) {
        viewModelScope.launch {
            try {
                val votingId = uiState.value.dateVoting?.votingId ?: return@launch
                dateVotingService.addDateOption(votingId, date)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun onCloseVoting() {
        viewModelScope.launch {
            try {
                val votingId = uiState.value.dateVoting?.votingId ?: return@launch
                dateVotingService.closeVoting(votingId)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}

