package dk.zlatan.flotmand.util

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object StringProviderModule {
    @Provides
    fun provideStringProvider(
        @ApplicationContext context: Context,
    ): StringProvider = DefaultStringProvider(context)
}
