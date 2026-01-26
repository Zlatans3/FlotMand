package dk.zlatan.flotmand.Features.frontpage.datevotingDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.zlatan.flotmand.R
import dk.zlatan.flotmand.model.DateOption
import dk.zlatan.flotmand.model.DateVotingItem
import dk.zlatan.flotmand.model.User
import dk.zlatan.flotmand.model.service.AccountService
import dk.zlatan.flotmand.model.service.DateVotingService
import dk.zlatan.flotmand.util.StringProvider
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
    val publisherUser: User? = null,
    val votersByUserId: Map<String, User> = emptyMap()
)

@HiltViewModel(assistedFactory = DateVotingViewModel.Factory::class)
internal class DateVotingViewModel @AssistedInject constructor(
    private val dateVotingService: DateVotingService,
    @Assisted private val votingId: String,
    private val accountService: AccountService,
    private val stringProvider: StringProvider,
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
                            fetchPublisherUser(updatedVoting)
                        }
                    }
                } else {
                    _uiState.update { it.copy(errorMessage = stringProvider.getString(R.string.error_voting_not_found), isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = stringProvider.getString(R.string.error_loading_voting, e.message ?: ""), isLoading = false) }
            }
        }
    }

    private fun fetchPublisherUser(voting: DateVotingItem) {
        viewModelScope.launch {
            try {
                val creatorId = voting.creatorId
                if (creatorId != null) {
                    val user = accountService.getUserById(creatorId)
                    _uiState.update { it.copy(publisherUser = user) }
                } else {
                    _uiState.update { it.copy(publisherUser = null) }
                }
            } catch (_: Exception) {
                // Don't update error state, this is non-critical
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
                _uiState.update { it.copy(snackbarMessage = stringProvider.getString(R.string.error_vote, e.message ?: "")) }
            }
        }
    }

    fun deleteVoting() {
        viewModelScope.launch {
            try {
                val votingId = uiState.value.dateVotingItem?.votingId ?: return@launch
                dateVotingService.deleteDateVoting(votingId)
            } catch (e: Exception) {
                _uiState.update { it.copy(snackbarMessage = stringProvider.getString(R.string.error_delete_voting, e.message ?: "")) }
            }
        }
    }

    fun onDeleteVotingOption(dateOption: DateOption) {
        viewModelScope.launch {
            try {
                val votingId = uiState.value.dateVotingItem?.votingId ?: return@launch
                dateVotingService.deleteVoteOption(dateOption, votingId)
            } catch (e: Exception) {
                _uiState.update { it.copy(snackbarMessage = stringProvider.getString(R.string.error_delete_date, e.message ?: "")) }
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
                _uiState.update { it.copy(snackbarMessage = stringProvider.getString(R.string.error_remove_vote, e.message ?: "")) }
            }
        }
    }

    fun onAddDateOption(date: LocalDate) {
        viewModelScope.launch {
            try {
                val votingId = uiState.value.dateVotingItem?.votingId
                if (votingId == null) {
                    _uiState.update { it.copy(snackbarMessage = stringProvider.getString(R.string.error_no_voting_found)) }
                    return@launch
                }

                val userId = accountService.currentUserId
                dateVotingService.addDateOption(votingId, date, userId)
            } catch (e: Exception) {
                _uiState.update { it.copy(snackbarMessage = stringProvider.getString(R.string.error_add_date, e.message ?: "")) }
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(eventId: String): DateVotingViewModel
    }
}
