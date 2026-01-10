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
 * Represents a standalone date voting session.
 * Users can propose and vote on dates. The voting creator can then create an event from it.
 *
 * @param votingId Unique identifier for this voting session
 * @param creatorId User ID of the person who created the voting
 * @param status Current status of the voting (OPEN, CLOSED)
 * @param dateOptions List of date options available for voting
 * @param createdAtString ISO-8601 timestamp of when voting was created
 */
data class DateVotingItem(
    @DocumentId
    val votingId: String? = null,
    val creatorId: String? = null,
    val status: VotingStatus = VotingStatus.OPEN,
    val dateOptions: List<DateOption> = emptyList(),
    val createdAtString: String? = null
) {
    @get:Exclude
    val winningDate: DateOption?
        get() = dateOptions.maxByOrNull { it.voteCount }

    @get:Exclude
    val totalVotes: Int
        get() = dateOptions.sumOf { it.voteCount }

    @get:Exclude
    val usersVoted: Int
        get() = dateOptions.flatMap { it.votersId }.toSet().size

    @get:Exclude
    val isOpen: Boolean
        get() = status == VotingStatus.OPEN

    fun getVotePercentage(dateOption: DateOption): Int {
        return if (totalVotes == 0) 0 else (dateOption.voteCount * 100) / totalVotes
    }

    fun addVote(date: LocalDate, userId: String): DateVotingItem {
        val updatedOptions = dateOptions.map { option ->
            if (option.localDate == date) {
                // Add vote if not already voted
                if (!option.votersId.contains(userId)) {
                    option.copy(votersId = option.votersId + userId)
                } else {
                    option
                }
            } else {
                // Keep votes on other dates - users can vote for multiple dates
                option
            }
        }
        return this.copy(dateOptions = updatedOptions)
    }

    fun removeVote(date: LocalDate, userId: String): DateVotingItem {
        val updatedOptions = dateOptions.map { option ->
            if (option.localDate == date) {
                option.copy(votersId = option.votersId.filterNot { it == userId })
            } else {
                option
            }
        }
        return this.copy(dateOptions = updatedOptions)
    }

    companion object {
        fun mockDateVotingItemCount(count: Int): DateVotingItem {
            val options = (1..count).map {
                DateOption(
                    date = LocalDate.now().plusDays(it.toLong()).toString(),
                    votersId = List(it) { "user_$it" }
                )
            }
            return DateVotingItem(
                votingId = "voting_mock",
                creatorId = "creator_mock",
                status = VotingStatus.OPEN,
                dateOptions = options,
                createdAtString = LocalDate.now().toString()
            )
        }

        fun mockDateVotingItemListCount(listCount: Int, optionCount: Int): List<DateVotingItem> {
            return (1..listCount).map {
                mockDateVotingItemCount(optionCount)
            }
        }
    }
}

enum class VotingStatus {
    OPEN,
    CLOSED
}
