package dk.zlatan.flotmand.Features.my_events.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dk.zlatan.flotmand.Features.my_events.navigaiton.MyEventsNavigationCoordinator
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MyEventsNavigationModule {
    @Binds
    @Singleton
    abstract fun bindMyEventsNavigationCoordinator(
        impl: MyEventsNavigationCoordinatorImpl
    ): MyEventsNavigationCoordinator
}