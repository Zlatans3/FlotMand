package dk.zlatan.flotmand.model.service

import dk.zlatan.flotmand.model.DateVoting
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface DateVotingService {
    /**
     * Observe a date voting session by its ID
     */
    fun observeVotingByIdFlow(votingId: String): Flow<DateVoting?>

    /**
     * Get all active date voting sessions
     */
    val allDateVotings: Flow<List<DateVoting>>

    /**
     * Create a new standalone date voting session
     */
    suspend fun createDateVoting(dateVoting: DateVoting): String

    /**
     * Get a specific date voting session by ID
     */
    suspend fun getDateVoting(votingId: String): DateVoting?

    /**
     * Update an existing date voting session
     */
    suspend fun updateDateVoting(dateVoting: DateVoting)

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
