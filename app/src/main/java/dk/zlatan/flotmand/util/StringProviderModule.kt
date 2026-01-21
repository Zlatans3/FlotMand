import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import dk.zlatan.flotmand.util.DefaultStringProvider
import dk.zlatan.flotmand.util.StringProvider

@Module
@InstallIn(SingletonComponent::class)
object StringProviderModule {
    @Provides
    fun provideStringProvider(@ApplicationContext context: Context): StringProvider = DefaultStringProvider(context)
}
// ...existing code...
