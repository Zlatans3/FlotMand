package dk.zlatan.flotmand.model

import com.google.firebase.firestore.DocumentId

data class User(
    @DocumentId
    val id: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val provider: String = "",
    val displayName: String = "",
    val photoUrl: String = "",
    val isAnonymous: Boolean = true,
) {
    fun getFirstName(): String = displayName.split(" ").firstOrNull() ?: displayName

    companion object {
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
