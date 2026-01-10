package dk.zlatan.flotmand.model.service

import dk.zlatan.flotmand.model.DateVoting
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface DateVotingService {
    /**
     * Observe a date voting session for a specific event
     */
    fun observeDateVoting(eventId: String): Flow<DateVoting?>

    /**
     * Get all date voting sessions
     */
    val allDateVotings: Flow<List<DateVoting>>

    /**
     * Create a new date voting session for an event
     */
    suspend fun createDateVoting(dateVoting: DateVoting): String

    /**
     * Get a date voting session by ID
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
     * Removes any existing vote by this user for other dates in this voting session
     */
    suspend fun addVote(votingId: String, date: LocalDate, userId: String)

    /**
     * Remove a vote for a specific date
     */
    suspend fun removeVote(votingId: String, date: LocalDate, userId: String)

    /**
     * Add a new date option to an existing voting session
     */
    suspend fun addDateOption(votingId: String, date: LocalDate)

    /**
     * Remove a date option from a voting session
     */
    suspend fun removeDateOption(votingId: String, date: LocalDate)

    /**
     * Close the voting and set the winning date
     */
    suspend fun closeVoting(votingId: String)
}
