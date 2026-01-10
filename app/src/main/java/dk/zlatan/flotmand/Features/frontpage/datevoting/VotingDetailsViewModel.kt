package dk.zlatan.flotmand.Features.frontpage.datevoting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.zlatan.flotmand.model.DateVotingItem
import dk.zlatan.flotmand.model.VotingStatus
import dk.zlatan.flotmand.model.service.AccountService
import dk.zlatan.flotmand.model.service.DateVotingService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.time.LocalDateTime

data class DateVotingListUiState(
    val votings: List<DateVotingItem> = emptyList(),
    val currentUserId: String = "",
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val snackbarMessage: String? = null,
    val newVotingId: String? = null,
    val showCreateVotingDialog: Boolean = false
)

@HiltViewModel
class DateVotingListViewModel @Inject constructor(
    private val dateVotingService: DateVotingService,
    private val accountService: AccountService
) : ViewModel() {

    private val _transientState = MutableStateFlow(
        TransientState()
    )

    val uiState: StateFlow<DateVotingListUiState> = combine(
        dateVotingService.allDateVotingsItem,
        _transientState
    ) { votings, transient ->
        DateVotingListUiState(
            votings = votings,
            currentUserId = accountService.currentUserId,
            isLoading = false,
            errorMessage = null,
            snackbarMessage = transient.snackbarMessage,
            newVotingId = transient.newVotingId,
            showCreateVotingDialog = transient.showCreateVotingDialog
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

    private data class TransientState(
        val snackbarMessage: String? = null,
        val newVotingId: String? = null,
        val showCreateVotingDialog: Boolean = false
    )

    fun showCreateVotingDialog() {
        _transientState.update { it.copy(showCreateVotingDialog = true) }
    }

    fun dismissCreateVotingDialog() {
        _transientState.update { it.copy(showCreateVotingDialog = false) }
    }

    fun createNewVoting(votingName: String = "") {
        viewModelScope.launch {
            try {
                _transientState.update { it.copy(snackbarMessage = null, showCreateVotingDialog = false) }

                val newVoting = DateVotingItem(
                    creatorId = accountService.currentUserId,
                    name = votingName.ifBlank { null },
                    status = VotingStatus.OPEN,
                    dateOptions = emptyList(),
                    createdAtString = LocalDateTime.now().toString()
                )

                val votingId = dateVotingService.createDateVoting(newVoting)

                _transientState.update {
                    it.copy(newVotingId = votingId)
                }
            } catch (e: Exception) {
                _transientState.update {
                    it.copy(snackbarMessage = "Kunne ikke oprette afstemning: ${e.message}")
                }
            }
        }
    }

    fun dismissSnackbar() {
        _transientState.update { it.copy(snackbarMessage = null) }
    }

    fun clearNewVotingId() {
        _transientState.update { it.copy(newVotingId = null) }
    }
}
