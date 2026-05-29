package dk.zlatan.flotmand.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class AppNotification(
    @DocumentId val id: String = "",
    val type: String = "",
    val referenceId: String = "",
    val title: String = "",
    val body: String? = null,
    @get:PropertyName("isRead") @set:PropertyName("isRead") var isRead: Boolean = false,
    val createdAtMillis: Long = 0L,
) {
    val notificationType: NotificationType get() = NotificationType.fromValue(type)
}

enum class NotificationType(val value: String) {
    EVENT("event"),
    POLL("poll"),
    UNKNOWN("unknown"),
    ;

    companion object {
        fun fromValue(value: String): NotificationType = entries.firstOrNull { it.value == value } ?: UNKNOWN
    }
}
