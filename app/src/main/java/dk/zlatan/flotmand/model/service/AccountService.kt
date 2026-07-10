package dk.zlatan.flotmand.model.service

import android.net.Uri
import dk.zlatan.flotmand.model.User
import kotlinx.coroutines.flow.Flow

interface AccountService {
    val currentUser: Flow<User?>
    val currentUserId: String

    fun hasUser(): Boolean

    fun getUserProfile(): User

    suspend fun getUserById(userId: String): User?

    fun observeUserById(userId: String): Flow<User?>

    suspend fun getUsersByIds(userIds: List<String>): List<User>

    suspend fun createAnonymousAccount()

    suspend fun updateDisplayName(newDisplayName: String)

    suspend fun updatePhoneNumber(newPhoneNumber: String)

    /** Persists the user's bio (trimmed to [User.BIO_MAX_LENGTH] characters). */
    suspend fun updateBio(newBio: String)

    /** Marks the first-time profile setup as done so the user is routed straight to the main app. */
    suspend fun markProfileCompleted()

    suspend fun updateProfilePhoto(imageUri: Uri)

    suspend fun linkAccount(
        email: String,
        password: String,
    )

    suspend fun signInWithGoogle(idToken: String)

    suspend fun signIn(
        email: String,
        password: String,
    )

    suspend fun signOut()

    suspend fun deleteAccount()

    /** Persists [token] to Firestore so Cloud Functions can address this device. */
    suspend fun updateFcmToken(token: String)
}
