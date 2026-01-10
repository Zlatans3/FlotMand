package dk.zlatan.flotmand.Features.frontpage.datevotingDetail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.zlatan.flotmand.model.DateVoting
import dk.zlatan.flotmand.model.User
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
    val snackbarMessage: String? = null,
    val votersByUserId: Map<String, User> = emptyMap()
)

@HiltViewModel(assistedFactory = DateVotingViewModel.Factory::class)
internal class DateVotingViewModel @AssistedInject constructor(
    private val dateVotingService: DateVotingService,
    @Assisted private val votingId: String,
    private val accountService: AccountService,
) : ViewModel() {

    companion object {
        private const val TAG = "DateVotingViewModel"
    }

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
                            // Fetch voter data when voting updates
                            if (updatedVoting != null) {
                                fetchVoterData(updatedVoting)
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

    private fun fetchVoterData(voting: DateVoting) {
        viewModelScope.launch {
            try {
                // Collect all unique voter IDs
                val allVoterIds = voting.dateOptions
                    .flatMap { it.votersId }
                    .distinct()

                if (allVoterIds.isEmpty()) {
                    _uiState.update { it.copy(votersByUserId = emptyMap()) }
                    return@launch
                }

                // Fetch actual user data for all voters
                val voters = accountService.getUsersByIds(allVoterIds)
                val voterMap = voters.associateBy { it.id }

                Log.d(TAG, "Fetched voter data for ${voterMap.size} voters")

                _uiState.update { it.copy(votersByUserId = voterMap) }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching voter data: ${e.message}", e)
                // Don't update error state, this is non-critical
            }
        }
    }

    fun dismissSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun onVoteForDate(date: LocalDate) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Adding vote for date: $date")
                val votingId = uiState.value.dateVoting?.votingId ?: return@launch
                val userId = accountService.currentUserId
                dateVotingService.addVote(votingId, date, userId)
                Log.d(TAG, "Vote added successfully for date: $date")
            } catch (e: Exception) {
                Log.e(TAG, "Error adding vote: ${e.message}", e)
                _uiState.update { it.copy(snackbarMessage = "Kunne ikke stemme: ${e.message}") }
            }
        }
    }

    fun deleteVoting() {
        viewModelScope.launch {
            try {
                val votingId = uiState.value.dateVoting?.votingId ?: return@launch
                dateVotingService.deleteDateVoting(votingId)
            } catch (e: Exception) {
                _uiState.update { it.copy(snackbarMessage = "Kunne ikke slette afstemning: ${e.message}") }
            }
        }
    }

    fun onRemoveVote(date: LocalDate) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Removing vote for date: $date")
                val votingId = uiState.value.dateVoting?.votingId ?: return@launch
                val userId = accountService.currentUserId
                dateVotingService.removeVote(votingId, date, userId)
                Log.d(TAG, "Vote removed successfully for date: $date")
            } catch (e: Exception) {
                Log.e(TAG, "Error removing vote: ${e.message}", e)
                _uiState.update { it.copy(snackbarMessage = "Kunne ikke fjerne stemme: ${e.message}") }
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

                val userId = accountService.currentUserId
                dateVotingService.addDateOption(votingId, date, userId)
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
