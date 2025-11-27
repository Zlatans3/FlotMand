package dk.zlatan.flotmand.impl

import com.google.firebase.firestore.dataObjects
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.Firebase
import dk.zlatan.flotmand.model.Event
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.tasks.await
import dk.zlatan.flotmand.model.service.AccountService
import dk.zlatan.flotmand.model.service.DinnerEventService
import jakarta.inject.Inject

class DinnerEventServiceImpl @Inject constructor(private val auth: AccountService) : DinnerEventService {

    @OptIn(ExperimentalCoroutinesApi::class)
    override val dinnerEvents: Flow<List<Event>>
        get() =
            auth.currentUser.flatMapLatest { note ->
                Firebase.firestore
                    .collection(NOTES_COLLECTION)
                    .whereEqualTo(USER_ID_FIELD, note?.id)
                    .dataObjects()
            }

    override suspend fun createDinnerEvent(event: Event) {
        val eventWithUserId = event.copy(publisherId = auth.currentUserId)
        Firebase.firestore
            .collection(NOTES_COLLECTION)
            .add(eventWithUserId).await()
    }

    override suspend fun readDinnerEvent(noteId: String): Event? {
        return Firebase.firestore
            .collection(NOTES_COLLECTION)
            .document(noteId).get().await().toObject()
    }

    override suspend fun updateDinnerEvent(event: Event) {
        Firebase.firestore
            .collection(NOTES_COLLECTION)
            // TODO: Zlatan 27/11/2025 Probably wrong .orEmpty()
            .document(event.eventId.orEmpty()).set(event).await()
    }

    override suspend fun deleteDinnerEvent(dinnerEventId: String) {
        Firebase.firestore
            .collection(NOTES_COLLECTION)
            .document(dinnerEventId).delete().await()
    }

    companion object {
        private const val USER_ID_FIELD = "userId"
        private const val NOTES_COLLECTION = "notes"
    }
}