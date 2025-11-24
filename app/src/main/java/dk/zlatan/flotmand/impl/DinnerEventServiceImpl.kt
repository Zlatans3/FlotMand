package dk.zlatan.flotmand.impl

import com.google.firebase.firestore.dataObjects
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.Firebase
import dk.zlatan.flotmand.model.DinnerEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.tasks.await
import dk.zlatan.flotmand.model.service.AccountService
import dk.zlatan.flotmand.model.service.DinnerEventService
import jakarta.inject.Inject

class DinnerEventServiceImpl @Inject constructor(private val auth: AccountService) : DinnerEventService {

    @OptIn(ExperimentalCoroutinesApi::class)
    override val dinnerEvents: Flow<List<DinnerEvent>>
        get() =
            auth.currentUser.flatMapLatest { note ->
                Firebase.firestore
                    .collection(NOTES_COLLECTION)
                    .whereEqualTo(USER_ID_FIELD, note?.id)
                    .dataObjects()
            }

    override suspend fun createDinnerEvent(note: DinnerEvent) {
        val noteWithUserId = note.copy(userId = auth.currentUserId)
        Firebase.firestore
            .collection(NOTES_COLLECTION)
            .add(noteWithUserId).await()
    }

    override suspend fun readDinnerEvent(noteId: String): DinnerEvent? {
        return Firebase.firestore
            .collection(NOTES_COLLECTION)
            .document(noteId).get().await().toObject()
    }

    override suspend fun updateDinnerEvent(note: DinnerEvent) {
        Firebase.firestore
            .collection(NOTES_COLLECTION)
            .document(note.id).set(note).await()
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