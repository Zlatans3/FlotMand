package dk.zlatan.flotmand.Features.frontpage.datevoting

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
    dateVotingService: DateVotingService,
    private val accountService: AccountService,
    private val dateVotingServiceImpl: DateVotingService
) : ViewModel() {

    private val _uiState = MutableStateFlow<DateVotingListUiState>(
        DateVotingListUiState(
            currentUserId = accountService.currentUserId,
            isLoading = true
        )
    )


    // TODO: Zlatan 10/01/2026 Could be combine flow
    val uiState: StateFlow<DateVotingListUiState> = dateVotingService.allDateVotings
        .map { votings ->
            _uiState.value.copy(
                votings = votings,
                currentUserId = accountService.currentUserId,
                isLoading = false,
                errorMessage = null
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
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(snackbarMessage = null) }

                val newVoting = DateVoting(
                    creatorId = accountService.currentUserId,
                    status = VotingStatus.OPEN,
                    dateOptions = emptyList(),
                    createdAtString = LocalDateTime.now().toString()
                )

                val votingId = dateVotingServiceImpl.createDateVoting(newVoting)

                _uiState.update {
                    it.copy(
                        snackbarMessage = "Afstemning oprettet",
                        newVotingId = votingId
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(snackbarMessage = "Kunne ikke oprette afstemning: ${e.message}")
                }
            }
        }
    }

    fun dismissSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun clearNewVotingId() {
        _uiState.update { it.copy(newVotingId = null) }
    }
}
