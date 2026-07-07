package dk.zlatan.flotmand.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class User(
    @DocumentId
    val id: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val provider: String = "",
    val displayName: String = "",
    val photoUrl: String = "",
    val bio: String = "",
    val isAnonymous: Boolean = true,
    @field:PropertyName("isGhostUser")
    @get:PropertyName("isGhostUser")
    val isGhostUser: Boolean = false,
    val fcmToken: String = "",
) {
    fun getFirstName(): String = displayName.split(" ").firstOrNull() ?: displayName

    companion object {
        const val BIO_MAX_LENGTH = 140

        fun mockUserWithCounter(counter: Int): List<User> =
            List(counter) { index ->
                val names =
                    listOf(
                        "Zlatan Stadler",
                        "Gustav Rasslan",
                        "Mikkel Rahbek",
                        "David Sandell",
                        "Oliver Payne",
                        "Lasse Sandø",
                    )
                User(
                    id = "user${index + 1}",
                    email = "user$index@gmail.com",
                    provider = "mockProvider",
                    displayName = names.random(),
                    phoneNumber = "12345678",
                    isAnonymous = false,
                )
            }
    }
}
