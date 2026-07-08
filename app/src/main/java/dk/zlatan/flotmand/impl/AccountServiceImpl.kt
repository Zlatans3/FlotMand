package dk.zlatan.flotmand.impl

import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import com.google.firebase.Firebase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.storage
import dk.zlatan.flotmand.model.User
import dk.zlatan.flotmand.model.service.AccountService
import dk.zlatan.flotmand.util.SessionPrefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AccountServiceImpl
    @Inject
    constructor(
        private val sessionPrefs: SessionPrefs,
    ) : AccountService {
        init {
            // If the user is already signed in when the app starts (e.g. after an app restart),
            // saveFcmToken() is never called via signIn/signInWithGoogle.
            // This ensures the token is always fresh in Firestore on every launch.
            if (hasUser()) {
                CoroutineScope(Dispatchers.IO).launch { saveFcmToken() }
            }
        }

        // Auth state decides *who* is signed in; the user's Firestore doc is then observed
        // with a snapshot listener. Latency compensation fires the listener immediately from
        // the local cache on every write, so profile mutations (name, phone, bio, photo,
        // profileCompleted) propagate to collectors without any manual re-emission — and
        // changes made from other devices or Cloud Functions propagate too.
        @OptIn(ExperimentalCoroutinesApi::class)
        override val currentUser: Flow<User?>
            get() =
                callbackFlow {
                    val listener = FirebaseAuth.AuthStateListener { auth -> trySend(auth.currentUser) }
                    Firebase.auth.addAuthStateListener(listener)
                    awaitClose { Firebase.auth.removeAuthStateListener(listener) }
                }.flatMapLatest { firebaseUser ->
                    if (firebaseUser == null) {
                        flowOf(null)
                    } else {
                        observeUserById(firebaseUser.uid)
                            .map { firestoreUser ->
                                // toNotesUser() re-reads the live FirebaseUser on every snapshot,
                                // so auth-profile changes are picked up as long as the mutation
                                // also touches the Firestore doc (all of ours do).
                                firebaseUser.toNotesUser().copy(
                                    phoneNumber = firestoreUser?.phoneNumber.orEmpty(),
                                    bio = firestoreUser?.bio.orEmpty(),
                                    profileCompleted = firestoreUser?.profileCompleted,
                                )
                            }.catch { e ->
                                // E.g. a permission error racing sign-out; fall back to
                                // auth-only data instead of killing collectors.
                                Log.w(TAG, "users/${firebaseUser.uid} listener failed: ${e.message}")
                                emit(firebaseUser.toNotesUser())
                            }
                    }
                }

        override val currentUserId: String
            get() =
                Firebase.auth.currentUser
                    ?.uid
                    .orEmpty()

        override fun hasUser(): Boolean = Firebase.auth.currentUser != null

        override fun getUserProfile(): User = Firebase.auth.currentUser.toNotesUser()

        override fun observeUserById(userId: String): Flow<User?> =
            callbackFlow {
                val reg =
                    Firebase.firestore
                        .collection(USERS_COLLECTION)
                        .document(userId)
                        .addSnapshotListener { snap, err ->
                            if (err != null) {
                                close(err)
                                return@addSnapshotListener
                            }
                            trySend(snap?.toObject<User>())
                        }
                awaitClose { reg.remove() }
            }

        override suspend fun getUserById(userId: String): User? =
            try {
                Firebase.firestore
                    .collection(USERS_COLLECTION)
                    .document(userId)
                    .get()
                    .await()
                    .toObject()
            } catch (_: Exception) {
                null
            }

        override suspend fun getUsersByIds(userIds: List<String>): List<User> {
            if (userIds.isEmpty()) return emptyList()

            return try {
                Log.d(TAG, "Fetching users for IDs: $userIds")
                // Firestore 'in' query supports up to 10 items; chunk to stay within the limit.
                val users = mutableListOf<User>()
                userIds.chunked(10).forEach { chunk ->
                    val querySnapshot =
                        Firebase.firestore
                            .collection(USERS_COLLECTION)
                            .whereIn(
                                com.google.firebase.firestore.FieldPath
                                    .documentId(),
                                chunk,
                            ).get()
                            .await()

                    Log.d(
                        TAG,
                        "Query returned ${querySnapshot.documents.size} documents for chunk: $chunk",
                    )
                    querySnapshot.documents.forEach { document ->
                        Log.d(TAG, "Document: ${document.id}, exists: ${document.exists()}")
                        document.toObject<User>()?.let {
                            users.add(it)
                            Log.d(TAG, "Parsed user: ${it.displayName}")
                        } ?: Log.w(TAG, "Failed to parse user from document ${document.id}")
                    }
                }
                Log.d(TAG, "Total users fetched: ${users.size}")
                users
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching users: ${e.message}", e)
                emptyList()
            }
        }

        override suspend fun createAnonymousAccount() {
            Firebase.auth.signInAnonymously().await()
            saveUserToFirestore()
        }

        private suspend fun saveUserToFirestore() {
            val currentUser = Firebase.auth.currentUser ?: return
            Log.d(TAG, "Saving user to Firestore: ${currentUser.uid}")
            try {
                val docRef =
                    Firebase.firestore
                        .collection(USERS_COLLECTION)
                        .document(currentUser.uid)
                val updates =
                    mutableMapOf(
                        "email" to (currentUser.email.orEmpty()),
                        "displayName" to (currentUser.displayName.orEmpty()),
                        "provider" to currentUser.providerId,
                        "isAnonymous" to currentUser.isAnonymous,
                    )
                val photoUrl = currentUser.photoUrl?.toString().orEmpty()
                if (photoUrl.isNotBlank()) updates["photoUrl"] = photoUrl
                // Brand-new users (no doc yet) start with profileCompleted = false so they
                // are routed through first-time profile setup. Docs that already exist are
                // left untouched: absence of the field means the user predates the feature.
                val isNewUser = !docRef.get().await().exists()
                if (isNewUser) updates["profileCompleted"] = false
                docRef
                    .set(updates, SetOptions.merge())
                    .await()
                Log.d(TAG, "User saved successfully to Firestore (isNewUser=$isNewUser)")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save user to Firestore: ${e.message}", e)
            }
        }

        override suspend fun updateDisplayName(newDisplayName: String) {
            val profileUpdates =
                userProfileChangeRequest {
                    displayName = newDisplayName
                }

            Firebase.auth.currentUser!!
                .updateProfile(profileUpdates)
                .await()
            saveUserToFirestore()
        }

        override suspend fun updatePhoneNumber(newPhoneNumber: String) {
            val uid = currentUserId
            Firebase.firestore
                .collection(USERS_COLLECTION)
                .document(uid)
                .update("phoneNumber", newPhoneNumber)
                .await()
        }

        override suspend fun updateBio(newBio: String) {
            val uid = currentUserId
            if (uid.isBlank()) return
            val trimmedBio = newBio.trim().take(User.BIO_MAX_LENGTH)
            Firebase.firestore
                .collection(USERS_COLLECTION)
                .document(uid)
                .update("bio", trimmedBio)
                .await()
        }

        override suspend fun markProfileCompleted() {
            val uid = currentUserId
            if (uid.isBlank()) return
            // Not awaited: the local write fires the currentUser snapshot listener
            // immediately (latency compensation) and syncs to the server when online,
            // so navigating to the main app isn't gated on a network round-trip.
            Firebase.firestore
                .collection(USERS_COLLECTION)
                .document(uid)
                .set(mapOf("profileCompleted" to true), SetOptions.merge())
        }

        override suspend fun updateProfilePhoto(imageUri: Uri) {
            val uid = currentUserId
            val storageRef = Firebase.storage.reference.child("profile_images/$uid/photo.jpg")
            storageRef.putFile(imageUri).await()
            val downloadUrl = storageRef.downloadUrl.await().toString()

            val profileUpdates = userProfileChangeRequest { photoUri = downloadUrl.toUri() }
            Firebase.auth.currentUser!!
                .updateProfile(profileUpdates)
                .await()

            Firebase.firestore
                .collection(USERS_COLLECTION)
                .document(uid)
                .update("photoUrl", downloadUrl)
                .await()
        }

        override suspend fun linkAccount(
            email: String,
            password: String,
        ) {
            val credential = EmailAuthProvider.getCredential(email, password)
            Firebase.auth.currentUser!!
                .linkWithCredential(credential)
                .await()
            saveUserToFirestore()
        }

        override suspend fun signIn(
            email: String,
            password: String,
        ) {
            Firebase.auth.signInWithEmailAndPassword(email, password).await()
            saveUserToFirestore()
            saveFcmToken()
        }

        override suspend fun signOut() {
            Firebase.auth.signOut()
            // Session-scoped flags (e.g. the notification-permission prompt)
            // start fresh for the next login.
            sessionPrefs.clear()
        }

        override suspend fun signInWithGoogle(idToken: String) {
            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
            Firebase.auth.signInWithCredential(firebaseCredential).await()
            saveUserToFirestore()
            saveFcmToken()
        }

        override suspend fun deleteAccount() {
            Firebase.auth.currentUser!!
                .delete()
                .await()
            // Deleting the account implicitly signs out — reset session flags too.
            sessionPrefs.clear()
        }

        override suspend fun updateFcmToken(token: String) {
            val uid = currentUserId
            if (uid.isBlank()) return
            Firebase.firestore
                .collection(USERS_COLLECTION)
                .document(uid)
                .update(FCM_TOKEN_FIELD, token)
                .await()
        }

        private suspend fun saveFcmToken() {
            try {
                val token = FirebaseMessaging.getInstance().token.await()
                updateFcmToken(token)
            } catch (e: Exception) {
                Log.w(TAG, "Could not fetch or save FCM token: ${e.message}")
            }
        }

        private fun FirebaseUser?.toNotesUser(): User =
            if (this == null) {
                User(
                    id = "",
                    email = "",
                    provider = "",
                    phoneNumber = "",
                    displayName = "",
                    photoUrl = "",
                    isAnonymous = true,
                )
            } else {
                User(
                    id = this.uid,
                    email = this.email.orEmpty(),
                    provider = this.providerId,
                    phoneNumber = this.phoneNumber.orEmpty(),
                    displayName = this.displayName.orEmpty(),
                    photoUrl = this.photoUrl?.toString().orEmpty(),
                    isAnonymous = this.isAnonymous,
                )
            }

        companion object {
            private const val USERS_COLLECTION = "users"
            private const val FCM_TOKEN_FIELD = "fcmToken"
            private const val TAG = "AccountService"
        }
    }
