package dk.zlatan.flotmand.Features.polls.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dk.zlatan.flotmand.Features.polls.navigation.PollsNavigationCoordinator
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PollsNavigationModule {
    @Binds
    @Singleton
    abstract fun bindPollsNavigationCoordinator(
        impl: PollsNavigationCoordinatorImpl,
    ): PollsNavigationCoordinator
}
