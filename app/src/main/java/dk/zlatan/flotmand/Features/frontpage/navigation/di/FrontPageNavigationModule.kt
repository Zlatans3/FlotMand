package dk.zlatan.flotmand.Features.frontpage.navigation.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dk.zlatan.flotmand.Features.frontpage.navigation.FrontPageNavigationCoordinator
import dk.zlatan.flotmand.Features.frontpage.di.FrontPageNavigationCoordinatorImpl
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

