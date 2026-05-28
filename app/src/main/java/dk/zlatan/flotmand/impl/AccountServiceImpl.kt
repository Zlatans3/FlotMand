package dk.zlatan.flotmand.impl

import com.google.firebase.Firebase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import dk.zlatan.flotmand.model.User
import dk.zlatan.flotmand.model.service.AccountService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AccountServiceImpl
    @Inject
    constructor() : AccountService {
        private val _manualUserUpdates = MutableSharedFlow<User?>(replay = 0)

        init {
            // If the user is already signed in when the app starts (e.g. after an app restart),
            // saveFcmToken() is never called via signIn/signInWithGoogle.
            // This ensures the token is always fresh in Firestore on every launch.
            if (hasUser()) {
                CoroutineScope(Dispatchers.IO).launch { saveFcmToken() }
            }
        }

        override val currentUser: Flow<User?>
            get() =
                merge(
                    callbackFlow {
                        val listener =
                            FirebaseAuth.AuthStateListener { auth ->
                                this.trySend(auth.currentUser.toNotesUser())
                            }
                        Firebase.auth.addAuthStateListener(listener)
                        awaitClose { Firebase.auth.removeAuthStateListener(listener) }
                    },
                    _manualUserUpdates,
                )

        override val currentUserId: String
            get() =
                Firebase.auth.currentUser
                    ?.uid
                    .orEmpty()

        override fun hasUser(): Boolean = Firebase.auth.currentUser != null

        override fun getUserProfile(): User = Firebase.auth.currentUser.toNotesUser()

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

                    Log.d(TAG, "Query returned ${querySnapshot.documents.size} documents for chunk: $chunk")
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
                Firebase.firestore
                    .collection(USERS_COLLECTION)
                    .document(currentUser.uid)
                    .set(currentUser.toNotesUser())
                    .await()
                Log.d(TAG, "User saved successfully to Firestore")
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
            // Phone number updates require SMS verification via PhoneAuthProvider,
            // which is not yet implemented.
            throw UnsupportedOperationException(
                "Opdatering af telefonnummer kræver SMS-verifikation. " +
                    "Denne funktion er endnu ikke implementeret.",
            )
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
                android.util.Log.w(TAG, "Could not fetch or save FCM token: ${e.message}")
            }
        }

        override suspend fun reloadUser() {
            Firebase.auth.currentUser
                ?.reload()
                ?.await()
            // Manually emit the updated user to trigger UI update
            _manualUserUpdates.emit(Firebase.auth.currentUser.toNotesUser())
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
                    email = this.email ?: "",
                    provider = this.providerId,
                    phoneNumber = this.phoneNumber ?: "",
                    displayName = this.displayName ?: "",
                    photoUrl = this.photoUrl?.toString() ?: "",
                    isAnonymous = this.isAnonymous,
                )
            }

        companion object {
            private const val USERS_COLLECTION = "users"
            private const val FCM_TOKEN_FIELD = "fcmToken"
            private const val TAG = "AccountService"
        }
    }
