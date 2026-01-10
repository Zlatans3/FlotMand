package dk.zlatan.flotmand.Features.frontpage.datevoting

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.zlatan.flotmand.model.DateVoting
import dk.zlatan.flotmand.model.VotingStatus
import dk.zlatan.flotmand.model.service.AccountService
import dk.zlatan.flotmand.model.service.DateVotingService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.time.LocalDateTime

data class DateVotingListUiState(
    val votings: List<DateVoting> = emptyList(),
    val currentUserId: String = "",
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val snackbarMessage: String? = null,
    val newVotingId: String? = null
)

@HiltViewModel
class DateVotingListViewModel @Inject constructor(
    private val dateVotingService: DateVotingService,
    private val accountService: AccountService
) : ViewModel() {

    companion object {
        private const val TAG = "DateVotingListViewModel"
    }

    private val _uiState = MutableStateFlow<DateVotingListUiState>(
        DateVotingListUiState(
            currentUserId = accountService.currentUserId,
            isLoading = true
        )
    )

    val uiState: StateFlow<DateVotingListUiState> = dateVotingService.allDateVotings
        .map { votings ->
            Log.d(TAG, "Received votings from service: ${votings.size} votings")
            DateVotingListUiState(
                votings = votings,
                currentUserId = accountService.currentUserId,
                isLoading = false,
                errorMessage = null,
                snackbarMessage = _uiState.value.snackbarMessage,
                newVotingId = _uiState.value.newVotingId
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DateVotingListUiState(
                currentUserId = accountService.currentUserId,
                isLoading = true
            )
        )

    fun createNewVoting() {
        Log.d(TAG, "createNewVoting called")
        viewModelScope.launch {
            try {
                Log.d(TAG, "Creating new voting - Current user ID: ${accountService.currentUserId}")
                _uiState.update { it.copy(snackbarMessage = null) }

                val newVoting = DateVoting(
                    creatorId = accountService.currentUserId,
                    status = VotingStatus.OPEN,
                    dateOptions = emptyList(),
                    createdAtString = LocalDateTime.now().toString()
                )

                Log.d(TAG, "New voting object created: votingId=${newVoting.votingId}, creatorId=${newVoting.creatorId}, createdAt=${newVoting.createdAtString}")

                val votingId = dateVotingService.createDateVoting(newVoting)

                Log.d(TAG, "Voting created successfully with ID: $votingId")

                _uiState.update {
                    Log.d(TAG, "Updating UI state with newVotingId: $votingId")
                    it.copy(
                        snackbarMessage = "Afstemning oprettet",
                        newVotingId = votingId
                    )
                }

                Log.d(TAG, "UI state updated. Current uiState: ${_uiState.value}")

                // Log all current votings after creation
                Log.d(TAG, "Current votings after creation:")
                Log.d(TAG, "Total votings: ${_uiState.value.votings.size}")
                _uiState.value.votings.forEachIndexed { index, voting ->
                    Log.d(TAG, "  [$index] votingId=${voting.votingId}, creatorId=${voting.creatorId}, dateOptions=${voting.dateOptions.size}, status=${voting.status}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error creating voting: ${e.message}", e)
                _uiState.update {
                    it.copy(snackbarMessage = "Kunne ikke oprette afstemning: ${e.message}")
                }
            }
        }
    }

    fun dismissSnackbar() {
        Log.d(TAG, "dismissSnackbar called")
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun clearNewVotingId() {
        Log.d(TAG, "clearNewVotingId called")
        _uiState.update { it.copy(newVotingId = null) }
    }
}
