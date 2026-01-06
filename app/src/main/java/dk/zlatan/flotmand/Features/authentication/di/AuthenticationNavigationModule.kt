package dk.zlatan.flotmand.Features.authentication.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dk.zlatan.flotmand.Features.authentication.navigation.AuthenticationNavigationCoordinator
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthenticationNavigationModule {
    @Binds
    @Singleton
    abstract fun bindAuthenticationNavigationCoordinator(
        impl: AuthenticationNavigationCoordinatorImpl
    ): AuthenticationNavigationCoordinator
}