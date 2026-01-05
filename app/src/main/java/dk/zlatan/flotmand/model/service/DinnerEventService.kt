package dk.zlatan.flotmand.model.service

import dk.zlatan.flotmand.model.Event
import kotlinx.coroutines.flow.Flow


interface DinnerEventService {
    val dinnerEvents: Flow<List<Event>>
    suspend fun createDinnerEvent(dinnerEvent: Event)
    suspend fun readDinnerEvent(dinnerEventId: String): Event?
    suspend fun updateDinnerEvent(dinnerEvent: Event)
    suspend fun deleteDinnerEvent(dinnerEventId: String)
}