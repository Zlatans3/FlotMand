package dk.zlatan.flotmand.model.service

import dk.zlatan.flotmand.model.DateOption
import dk.zlatan.flotmand.model.DateVotingItem
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface DateVotingService {
    /**
     * Observe a date voting session by its ID
     */
    fun observeVotingByIdFlow(votingId: String): Flow<DateVotingItem?>

    /**
     * Get all active date voting sessions
     */
    val allDateVotingsItem: Flow<List<DateVotingItem>>

    /**
     * Create a new standalone date voting session
     */
    suspend fun createDateVoting(dateVotingItem: DateVotingItem): String

    /**
     * Get a specific date voting session by ID
     */
    suspend fun getDateVoting(votingId: String): DateVotingItem?

    /**
     * Update an existing date voting session
     */
    suspend fun updateDateVoting(dateVotingItem: DateVotingItem)

    /**
     * Delete a date voting session
     */
    suspend fun deleteDateVoting(votingId: String)

    /**
     * Add a vote for a specific date
     */
    suspend fun addVote(votingId: String, date: LocalDate, userId: String)

    /**
     * Remove a vote for a specific date
     */
    suspend fun removeVote(votingId: String, date: LocalDate, userId: String)

    suspend fun deleteVoteOption(voteOption: DateOption, votingId: String)

    /**
     * Add a new date option to an existing voting session
     * The userId will be automatically added as a voter for this date
     */
    suspend fun addDateOption(votingId: String, date: LocalDate, userId: String)

    /**
     * Close the voting session
     */
    suspend fun closeVoting(votingId: String)
}
