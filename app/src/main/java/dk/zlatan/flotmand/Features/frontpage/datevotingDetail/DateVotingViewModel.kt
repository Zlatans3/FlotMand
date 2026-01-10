package dk.zlatan.flotmand.Features.frontpage.datevotingDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.zlatan.flotmand.model.DateVotingItem
import dk.zlatan.flotmand.model.User
import dk.zlatan.flotmand.model.service.AccountService
import dk.zlatan.flotmand.model.service.DateVotingService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class DateVotingUiState(
    val dateVotingItem: DateVotingItem? = null,
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


    private val _uiState: MutableStateFlow<DateVotingUiState> = MutableStateFlow(
        DateVotingUiState(
            currentUserId = accountService.currentUserId,
            isLoading = true
        )
    )

    val uiState: StateFlow<DateVotingUiState> = _uiState

    init {
        viewModelScope.launch {
            try {
                val voting = dateVotingService.getDateVoting(votingId)
                if (voting != null) {
                    dateVotingService.observeVotingByIdFlow(votingId).collect { updatedVoting ->
                        _uiState.update { currentState ->
                            currentState.copy(
                                dateVotingItem = updatedVoting,
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

    private fun fetchVoterData(voting: DateVotingItem) {
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

                _uiState.update { it.copy(votersByUserId = voterMap) }
            } catch (_: Exception) {
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
                val votingId = uiState.value.dateVotingItem?.votingId ?: return@launch
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
                val votingId = uiState.value.dateVotingItem?.votingId ?: return@launch
                dateVotingService.deleteDateVoting(votingId)
            } catch (e: Exception) {
                _uiState.update { it.copy(snackbarMessage = "Kunne ikke slette afstemning: ${e.message}") }
            }
        }
    }

    fun onRemoveVote(date: LocalDate) {
        viewModelScope.launch {
            try {
                val votingId = uiState.value.dateVotingItem?.votingId ?: return@launch
                val userId = accountService.currentUserId
                dateVotingService.removeVote(votingId, date, userId)
            } catch (e: Exception) {
                _uiState.update { it.copy(snackbarMessage = "Kunne ikke fjerne stemme: ${e.message}") }
            }
        }
    }

    fun onAddDateOption(date: LocalDate) {
        viewModelScope.launch {
            try {
                val votingId = uiState.value.dateVotingItem?.votingId
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

    @AssistedFactory
    interface Factory {
        fun create(eventId: String): DateVotingViewModel
    }
}
