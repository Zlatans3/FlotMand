package dk.zlatan.flotmand.Features.frontpage.datevoting

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.zlatan.flotmand.model.DateVoting
import dk.zlatan.flotmand.model.service.AccountService
import dk.zlatan.flotmand.model.service.DateVotingService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class DateVotingUiState(
    val dateVoting: DateVoting? = null,
    val currentUserId: String = "",
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val snackbarMessage: String? = null
)

@HiltViewModel
class DateVotingViewModel @Inject constructor(
    private val dateVotingService: DateVotingService,
    private val accountService: AccountService,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val eventId: String? = savedStateHandle["eventId"]

    private val _uiState: MutableStateFlow<DateVotingUiState> = if (eventId != null) {
        MutableStateFlow(
            DateVotingUiState(
                currentUserId = accountService.currentUserId,
                isLoading = true
            )
        )
    } else {
        MutableStateFlow(
            DateVotingUiState(
                dateVoting = null,
                currentUserId = accountService.currentUserId,
                isLoading = false,
                errorMessage = "No event ID provided"
            )
        )
    }

    val uiState: StateFlow<DateVotingUiState> = _uiState

    init {
        if (eventId != null) {
            viewModelScope.launch {
                dateVotingService.observeDateVoting(eventId).collect { voting ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            dateVoting = voting,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
            }
        }
    }

    fun dismissSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

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
                val votingId = uiState.value.dateVoting?.votingId
                if (votingId == null) {
                    _uiState.update { it.copy(snackbarMessage = "Ingen afstemning fundet") }
                    return@launch
                }

                dateVotingService.addDateOption(votingId, date)
                _uiState.update { it.copy(snackbarMessage = "Dato tilføjet") }
            } catch (e: Exception) {
                _uiState.update { it.copy(snackbarMessage = "Kunne ikke tilføje dato: ${e.message}") }
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

