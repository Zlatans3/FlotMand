package dk.zlatan.flotmand.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import java.time.LocalDate

/**
 * Represents a single date option in a date voting session.
 *
 * @param date The proposed date for the event
 * @param votersId List of user IDs who voted for this date
 */
data class DateOption(
    val date: String? = null, // Stored as ISO-8601 string for Firestore compatibility
    val votersId: List<String> = emptyList()
) {
    @get:Exclude
    val localDate: LocalDate?
        get() = date?.let { LocalDate.parse(it) }

    @get:Exclude
    val voteCount: Int
        get() = votersId.size

    companion object {
        fun fromLocalDate(date: LocalDate): DateOption {
            return DateOption(date = date.toString(), votersId = emptyList())
        }
    }
}

/**
 * Represents a date voting session for an event.
 * Users can propose and vote on dates for when an event should take place.
 *
 * @param votingId Unique identifier for this voting session
 * @param status Current status of the voting (OPEN, CLOSED)
 * @param dateOptions List of date options available for voting
 */
data class DateVoting(
    @DocumentId
    val votingId: String? = null,
    val status: VotingStatus = VotingStatus.OPEN,
    val dateOptions: List<DateOption> = emptyList(),
) {
    @get:Exclude
    val winningDate: DateOption?
        get() = dateOptions.maxByOrNull { it.voteCount }

    @get:Exclude
    val totalVotes: Int
        get() = dateOptions.sumOf { it.voteCount }

    @get:Exclude
    val isOpen: Boolean
        get() = status == VotingStatus.OPEN

    fun getVotePercentage(dateOption: DateOption): Int {
        return if (totalVotes == 0) 0 else (dateOption.voteCount * 100) / totalVotes
    }

    fun addVote(date: LocalDate, userId: String): DateVoting {
        val updatedOptions = dateOptions.map { option ->
            if (option.localDate == date) {
                // Add vote if not already voted
                if (!option.votersId.contains(userId)) {
                    option.copy(votersId = option.votersId + userId)
                } else {
                    option
                }
            } else {
                // Remove vote from other dates if user voted
                option.copy(votersId = option.votersId.filterNot { it == userId })
            }
        }
        return this.copy(dateOptions = updatedOptions)
    }

    fun removeVote(date: LocalDate, userId: String): DateVoting {
        val updatedOptions = dateOptions.map { option ->
            if (option.localDate == date) {
                option.copy(votersId = option.votersId.filterNot { it == userId })
            } else {
                option
            }
        }
        return this.copy(dateOptions = updatedOptions)
    }

    fun hasUserVoted(userId: String): Boolean {
        return dateOptions.any { it.votersId.contains(userId) }
    }

    fun getUserVote(userId: String): DateOption? {
        return dateOptions.firstOrNull { it.votersId.contains(userId) }
    }
}

enum class VotingStatus {
    OPEN,
    CLOSED
}
