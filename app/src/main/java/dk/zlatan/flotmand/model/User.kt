package dk.zlatan.flotmand.model

data class User(
    val id: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val provider: String = "",
    val displayName: String = "",
    val photoUrl: String = "",
    val isAnonymous: Boolean = true
) {
    companion object {
        fun mockUserWithCounter(counter: Int): List<User> {
            return List(counter) { index ->
                User(
                    id = "user${index + 1}",
                    email = "user${index}@gmail.com",
                            provider = "mockProvider",
                    displayName = "Mock User ${index + 1}",
                    isAnonymous = false
                )
            }
        }
    }
}