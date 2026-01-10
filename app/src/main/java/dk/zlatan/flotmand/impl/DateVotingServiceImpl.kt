package dk.zlatan.flotmand.impl

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import dk.zlatan.flotmand.model.DateOption
import dk.zlatan.flotmand.model.DateVoting
import dk.zlatan.flotmand.model.VotingStatus
import dk.zlatan.flotmand.model.service.DateVotingService
import jakarta.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.LocalDateTime

class DateVotingServiceImpl @Inject constructor() : DateVotingService {

    override fun observeVotingByIdFlow(votingId: String): Flow<DateVoting?> = callbackFlow {
        val docRef = Firebase.firestore
            .collection(DATE_VOTING_COLLECTION)
            .document(votingId)

        val registration = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error observing voting $votingId: ${error.message}", error)
                trySend(null)
                return@addSnapshotListener
            }

            if (snapshot == null || !snapshot.exists()) {
                Log.w(TAG, "Voting $votingId no longer exists")
                trySend(null)
                return@addSnapshotListener
            }

            val voting = safeParseDateVoting(snapshot)
            trySend(voting)
        }

        awaitClose { registration.remove() }
    }

    override val allDateVotings: Flow<List<DateVoting>>
        get() = callbackFlow {
            val listenerRegistration = Firebase.firestore
                .collection(DATE_VOTING_COLLECTION)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Error fetching all date votings: ${error.message}", error)
                        close(error)
                        return@addSnapshotListener
                    }

                    val votings = snapshot?.documents?.mapNotNull { doc ->
                        safeParseDateVoting(doc).also { voting ->
                            if (voting == null) Log.w(TAG, "Failed to parse document ${doc.id}")
                        }
                    } ?: emptyList()

                    Log.d(TAG, "Fetched ${votings.size} date votings from Firestore")
                    votings.forEachIndexed { index, voting ->
                        Log.d(TAG, "  [$index] votingId=${voting.votingId}, creatorId=${voting.creatorId}, dateOptions=${voting.dateOptions.size}, status=${voting.status}")
                    }

                    trySend(votings)
                }

            awaitClose { listenerRegistration.remove() }
        }

    override suspend fun createDateVoting(dateVoting: DateVoting): String {
        try {
            Log.d(TAG, "Creating new date voting with creator: ${dateVoting.creatorId}")

            val docRef = Firebase.firestore
                .collection(DATE_VOTING_COLLECTION)
                .add(dateVoting)
                .await()

            Log.d(TAG, "Successfully created date voting with ID: ${docRef.id}")
            return docRef.id
        } catch (e: Exception) {
            Log.e(TAG, "Error creating date voting: ${e.message}", e)
            throw e
        }
    }

    override suspend fun getDateVoting(votingId: String): DateVoting? {
        return try {
            val doc = Firebase.firestore
                .collection(DATE_VOTING_COLLECTION)
                .document(votingId)
                .get()
                .await()

            if (!doc.exists()) {
                Log.w(TAG, "Date voting not found: $votingId")
                return null
            }

            safeParseDateVoting(doc)
        } catch (e: Exception) {
            Log.e(TAG, "Error reading date voting $votingId: ${e.message}", e)
            null
        }
    }

    override suspend fun updateDateVoting(dateVoting: DateVoting) {
        try {
            Firebase.firestore
                .collection(DATE_VOTING_COLLECTION)
                .document(dateVoting.votingId.orEmpty())
                .set(dateVoting)
                .await()

            Log.d(TAG, "Updated date voting: ${dateVoting.votingId}")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating date voting ${dateVoting.votingId}: ${e.message}", e)
            throw e
        }
    }

    override suspend fun deleteDateVoting(votingId: String) {
        try {
            Firebase.firestore
                .collection(DATE_VOTING_COLLECTION)
                .document(votingId)
                .delete()
                .await()

            Log.d(TAG, "Deleted date voting: $votingId")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting date voting $votingId: ${e.message}", e)
            throw e
        }
    }

    override suspend fun addVote(votingId: String, date: LocalDate, userId: String) {
        try {
            val voting = getDateVoting(votingId) ?: return
            val updatedVoting = voting.addVote(date, userId)
            updateDateVoting(updatedVoting)

            Log.d(TAG, "User $userId voted for $date in voting $votingId")
        } catch (e: Exception) {
            Log.e(TAG, "Error adding vote: ${e.message}", e)
            throw e
        }
    }

    override suspend fun removeVote(votingId: String, date: LocalDate, userId: String) {
        try {
            val voting = getDateVoting(votingId) ?: return
            val updatedVoting = voting.removeVote(date, userId)
            updateDateVoting(updatedVoting)

            Log.d(TAG, "User $userId removed vote for $date in voting $votingId")
        } catch (e: Exception) {
            Log.e(TAG, "Error removing vote: ${e.message}", e)
            throw e
        }
    }

    override suspend fun addDateOption(votingId: String, date: LocalDate) {
        try {
            val voting = getDateVoting(votingId) ?: return

            // Check if date already exists
            if (voting.dateOptions.any { it.localDate == date }) {
                Log.w(TAG, "Date $date already exists in voting $votingId")
                return
            }

            val newOption = DateOption.fromLocalDate(date)
            val updatedVoting = voting.copy(
                dateOptions = voting.dateOptions + newOption
            )
            updateDateVoting(updatedVoting)

            Log.d(TAG, "Added date option $date to voting $votingId")
        } catch (e: Exception) {
            Log.e(TAG, "Error adding date option: ${e.message}", e)
            throw e
        }
    }


    override suspend fun closeVoting(votingId: String) {
        try {
            val voting = getDateVoting(votingId) ?: return

            val closedVoting = voting.copy(
                status = VotingStatus.CLOSED,
            )
            updateDateVoting(closedVoting)

            Log.d(TAG, "Closed voting $votingId")
        } catch (e: Exception) {
            Log.e(TAG, "Error closing voting: ${e.message}", e)
            throw e
        }
    }

    private fun safeParseDateVoting(doc: DocumentSnapshot): DateVoting? {
        return try {
            doc.toObject<DateVoting>()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse document ${doc.id}: ${e.message}", e)
            null
        }
    }

    companion object {
        private const val TAG = "DateVotingService"
        private const val DATE_VOTING_COLLECTION = "dateVotings"
        private const val EVENT_ID_FIELD = "eventId"
    }
}
