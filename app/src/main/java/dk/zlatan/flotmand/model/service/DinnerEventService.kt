package dk.zlatan.flotmand.model.service

import dk.zlatan.flotmand.model.Event
import kotlinx.coroutines.flow.Flow


interface DinnerEventService {
    // All events from all users
    val allDinnerEvents: Flow<List<Event>>

    // Current user's events only
    val dinnerEventsByUserId: Flow<List<Event>>
    suspend fun createDinnerEvent(dinnerEvent: Event)
    suspend fun readDinnerEvent(dinnerEventId: String): Event?
    suspend fun updateDinnerEvent(dinnerEvent: Event)
    suspend fun deleteDinnerEvent(dinnerEventId: String)
}