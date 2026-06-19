package dk.zlatan.flotmand.util

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Named

class PhoneDialogRepository @Inject constructor(
    @Named("user_prefs") private val dataStore: DataStore<Preferences>,
) {
    private val key = booleanPreferencesKey("phone_dialog_dismissed")

    val isDismissed: Flow<Boolean> = dataStore.data.map { it[key] ?: false }

    suspend fun dismiss() {
        dataStore.edit { it[key] = true }
    }

    suspend fun reset() {
        dataStore.edit { it[key] = false }
    }
}
