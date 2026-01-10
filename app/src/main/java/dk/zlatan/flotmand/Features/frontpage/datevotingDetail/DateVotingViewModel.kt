package dk.zlatan.flotmand.Features.frontpage.datevotingDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.zlatan.flotmand.model.DateVoting
import dk.zlatan.flotmand.model.service.AccountService
import dk.zlatan.flotmand.model.service.DateVotingService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class DateVotingUiState(
    val dateVoting: DateVoting? = null,
    val currentUserId: String = "",
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val snackbarMessage: String? = null
)

@HiltViewModel(assistedFactory = DateVotingViewModel.Factory::class)
internal class DateVotingViewModel @AssistedInject constructor(
    private val dateVotingService: DateVotingService,
    @Assisted private val votingId: String,
    private val accountService: AccountService,
) : ViewModel() {

    private val _uiState: MutableStateFlow<DateVotingUiState> = if (votingId != null) {
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
                errorMessage = "No voting ID provided"
            )
        )
    }

    val uiState: StateFlow<DateVotingUiState> = _uiState

    init {
        if (votingId != null) {
            viewModelScope.launch {
                try {
                    val voting = dateVotingService.getDateVoting(votingId)
                    if (voting != null) {
                        dateVotingService.observeVotingByIdFlow(votingId).collect { updatedVoting ->
                            _uiState.update { currentState ->
                                currentState.copy(
                                    dateVoting = updatedVoting,
                                    isLoading = false,
                                    errorMessage = null
                                )
                            }
                        }
                    } else {
                        _uiState.update { it.copy(errorMessage = "Voting not found", isLoading = false) }
                    }
                } catch (e: Exception) {
                    _uiState.update { it.copy(errorMessage = "Error loading voting: ${e.message}", isLoading = false) }
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
                _uiState.update { it.copy(snackbarMessage = "Kunne ikke stemme: ${e.message}") }
            }
        }
    }

    fun deleteVoting() {
        viewModelScope.launch {
            try {
                val votingId = uiState.value.dateVoting?.votingId ?: return@launch
                dateVotingService.deleteDateVoting(votingId)
                _uiState.update { it.copy(snackbarMessage = "Afstemning slettet") }
            } catch (e: Exception) {
                _uiState.update { it.copy(snackbarMessage = "Kunne ikke slette afstemning: ${e.message}") }
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

    @AssistedFactory
    interface Factory {
        fun create(eventId: String): DateVotingViewModel
    }
}
