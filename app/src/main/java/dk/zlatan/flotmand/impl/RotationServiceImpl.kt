package dk.zlatan.flotmand.impl

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import dk.zlatan.flotmand.model.Group
import dk.zlatan.flotmand.model.RotationMonth
import dk.zlatan.flotmand.model.User
import dk.zlatan.flotmand.model.service.RotationService
import dk.zlatan.flotmand.util.RotationCalculator
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

class RotationServiceImpl @Inject constructor() : RotationService {

    override fun observeGroup(groupId: String): Flow<Group?> =
        callbackFlow {
            val reg = Firebase.firestore
                .collection(GROUPS)
                .document(groupId)
                .addSnapshotListener { snap, err ->
                    if (err != null) {
                        Log.e(TAG, "observeGroup error: ${err.message}", err)
                        close(err)
                        return@addSnapshotListener
                    }
                    trySend(snap?.toObject<Group>())
                }
            awaitClose { reg.remove() }
        }

    override fun observeTimelineOverrides(groupId: String): Flow<List<RotationMonth>> =
        callbackFlow {
            val reg = Firebase.firestore
                .collection(GROUPS)
                .document(groupId)
                .collection(TIMELINE)
                .addSnapshotListener { snap, err ->
                    if (err != null) {
                        Log.e(TAG, "observeTimelineOverrides error: ${err.message}", err)
                        close(err)
                        return@addSnapshotListener
                    }
                    val months = snap?.documents?.mapNotNull { it.toObject<RotationMonth>() } ?: emptyList()
                    trySend(months)
                }
            awaitClose { reg.remove() }
        }

    override suspend fun dismissBanner(userId: String, monthId: String) {
        Firebase.firestore
            .collection(USERS)
            .document(userId)
            .update("dismissedBannerMonth", monthId)
            .await()
    }

    override suspend fun releaseMonth(groupId: String, monthId: String, releasedByUserId: String) {
        Firebase.firestore
            .collection(GROUPS)
            .document(groupId)
            .collection(TIMELINE)
            .document(monthId)
            .set(RotationMonth.vacantMonth(monthId, releasedByUserId, LocalDate.now().toString()))
            .await()
    }

    override suspend fun claimMonth(groupId: String, monthId: String, claimedByUserId: String) {
        val docRef = Firebase.firestore
            .collection(GROUPS)
            .document(groupId)
            .collection(TIMELINE)
            .document(monthId)

        Firebase.firestore.runTransaction { tx ->
            val snap = tx.get(docRef)
            val current = snap.toObject<RotationMonth>()
                ?: throw IllegalStateException("Timeline document not found: $monthId")
            if (current.status != RotationMonth.Status.VACANT) {
                throw IllegalStateException("Slot is no longer vacant: $monthId")
            }
            tx.set(
                docRef,
                RotationMonth.claimedMonth(
                    monthId = monthId,
                    releasedBy = current.releasedBy,
                    claimedBy = claimedByUserId,
                    claimedAt = LocalDate.now().toString(),
                ),
            )
        }.await()
    }

    override suspend fun addUserToRotation(groupId: String, userId: String) {
        val docRef = Firebase.firestore.collection(GROUPS).document(groupId)
        Firebase.firestore.runTransaction { tx ->
            val snap = tx.get(docRef)
            val existing = snap.toObject<Group>()
            if (existing != null) {
                if (userId in existing.rotationOrder) return@runTransaction
                val newOrder = existing.rotationOrder + userId
                val newMembers = if (userId in existing.members) existing.members else existing.members + userId
                val updates = mutableMapOf<String, Any>("rotationOrder" to newOrder, "members" to newMembers)
                if (existing.anchorMonth.isBlank()) {
                    updates["anchorMonth"] = RotationCalculator.currentMonthId(existing.timezone)
                    updates["anchorIndex"] = 0
                }
                tx.update(docRef, updates)
            } else {
                val newGroup = Group(
                    id = groupId,
                    rotationOrder = listOf(userId),
                    members = listOf(userId),
                    anchorMonth = RotationCalculator.currentMonthId(),
                    anchorIndex = 0,
                )
                tx.set(docRef, newGroup)
            }
        }.await()
    }

    override suspend fun removeUserFromRotation(groupId: String, userId: String) {
        val docRef = Firebase.firestore.collection(GROUPS).document(groupId)
        Firebase.firestore.runTransaction { tx ->
            val group = tx.get(docRef).toObject<Group>() ?: return@runTransaction
            val removedIndex = group.rotationOrder.indexOf(userId)
            if (removedIndex == -1) return@runTransaction
            val newOrder = group.rotationOrder.filter { it != userId }
            val newMembers = group.members.filter { it != userId }
            val newAnchorIndex = if (newOrder.isEmpty()) {
                0
            } else {
                val adjusted = if (removedIndex < group.anchorIndex) group.anchorIndex - 1 else group.anchorIndex
                adjusted % newOrder.size
            }
            tx.update(
                docRef,
                mapOf(
                    "rotationOrder" to newOrder,
                    "members" to newMembers,
                    "anchorIndex" to newAnchorIndex,
                ),
            )
        }.await()
        resetTimelineOverrides(groupId)
    }

    override suspend fun assignMonthHost(groupId: String, monthId: String, newHostId: String) {
        val now = LocalDate.now().toString()
        Firebase.firestore
            .collection(GROUPS)
            .document(groupId)
            .collection(TIMELINE)
            .document(monthId)
            .set(
                RotationMonth(
                    monthId = monthId,
                    status = RotationMonth.Status.CLAIMED,
                    releasedBy = "",
                    overrideHostId = newHostId,
                    claimedAt = now,
                    updatedAt = now,
                ),
            )
            .await()
    }

    override suspend fun createGhostUser(groupId: String, displayName: String) {
        val ghostId = UUID.randomUUID().toString()
        Firebase.firestore
            .collection(USERS)
            .document(ghostId)
            .set(User(displayName = displayName.trim(), isGhost = true, isAnonymous = false))
            .await()
        addUserToRotation(groupId, ghostId)
    }

    override suspend fun reorderRotation(groupId: String, newOrder: List<String>) {
        val docRef = Firebase.firestore.collection(GROUPS).document(groupId)
        Firebase.firestore.runTransaction { tx ->
            val group = tx.get(docRef).toObject<Group>() ?: return@runTransaction
            val deduped = newOrder.distinct()
            tx.update(
                docRef,
                mapOf(
                    "rotationOrder" to deduped,
                    "anchorIndex" to 0,
                    "anchorMonth" to RotationCalculator.currentMonthId(group.timezone),
                ),
            )
        }.await()
    }

    override suspend fun resetTimelineOverrides(groupId: String) {
        val docs = Firebase.firestore
            .collection(GROUPS)
            .document(groupId)
            .collection(TIMELINE)
            .get()
            .await()
        if (docs.isEmpty) return
        Firebase.firestore.runBatch { batch ->
            docs.documents.forEach { batch.delete(it.reference) }
        }.await()
    }

    companion object {
        private const val TAG = "RotationService"
        private const val GROUPS = "groups"
        private const val TIMELINE = "timeline"
        private const val USERS = "users"
    }
}
