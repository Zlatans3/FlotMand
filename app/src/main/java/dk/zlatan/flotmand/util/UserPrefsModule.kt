package dk.zlatan.flotmand.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

private val Context.userPrefsDataStore: DataStore<Preferences>
    by preferencesDataStore(name = "user_prefs")

@Module
@InstallIn(SingletonComponent::class)
object UserPrefsModule {

    @Provides
    @Singleton
    @Named("user_prefs")
    fun provideUserPrefsDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> = context.userPrefsDataStore
}
