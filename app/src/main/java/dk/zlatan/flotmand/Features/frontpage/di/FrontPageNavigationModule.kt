package dk.zlatan.flotmand.Features.frontpage.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dk.zlatan.flotmand.Features.frontpage.navigation.FrontPageNavigationCoordinator
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FrontPageNavigationModule {
    @Binds
    @Singleton
    abstract fun bindFrontPageNavigationCoordinator(
        impl: FrontPageNavigationCoordinatorImpl
    ): FrontPageNavigationCoordinator
}