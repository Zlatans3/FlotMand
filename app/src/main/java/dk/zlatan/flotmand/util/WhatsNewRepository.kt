package dk.zlatan.flotmand.util

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Named

class WhatsNewRepository
    @Inject
    constructor(
        @Named("user_prefs") private val dataStore: DataStore<Preferences>,
    ) {
        private val key = stringPreferencesKey("whats_new_last_seen_version")

        val lastSeenVersion: Flow<String?> = dataStore.data.map { it[key] }

        suspend fun markSeen(version: String) {
            dataStore.edit { it[key] = version }
        }

        suspend fun reset() {
            dataStore.edit { it.remove(key) }
        }
    }
