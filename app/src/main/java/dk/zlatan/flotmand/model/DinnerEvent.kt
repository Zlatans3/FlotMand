package dk.zlatan.flotmand.model

import com.google.firebase.firestore.DocumentId

private const val TITLE_MAX_SIZE = 30

data class DinnerEvent(
    @DocumentId val id: String = "",
    val text: String = "",
    val userId: String = ""
)

fun DinnerEvent.getTitle(): String {
    val isLongText = this.text.length > TITLE_MAX_SIZE
    val endRange = if (isLongText) TITLE_MAX_SIZE else this.text.length - 1
    return this.text.substring(IntRange(0, endRange))
}