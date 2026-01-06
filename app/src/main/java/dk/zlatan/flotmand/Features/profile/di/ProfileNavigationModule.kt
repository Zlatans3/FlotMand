package dk.zlatan.flotmand.Features.profile.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dk.zlatan.flotmand.Features.profile.navigation.ProfileNavigationCoordinator
import dk.zlatan.flotmand.Features.profile.di.ProfileNavigationCoordinatorImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ProfileNavigationModule {
    @Binds
    @Singleton
    abstract fun bindProfileNavigationCoordinator(
        impl: ProfileNavigationCoordinatorImpl
    ): ProfileNavigationCoordinator
}