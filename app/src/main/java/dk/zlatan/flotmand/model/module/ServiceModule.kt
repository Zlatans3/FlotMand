package dk.zlatan.flotmand.model.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dk.zlatan.flotmand.impl.AccountServiceImpl
import dk.zlatan.flotmand.impl.DateVotingServiceImpl
import dk.zlatan.flotmand.impl.DinnerEventServiceImpl
import dk.zlatan.flotmand.impl.NotificationServiceImpl
import dk.zlatan.flotmand.impl.PlacesServiceImpl
import dk.zlatan.flotmand.impl.RotationServiceImpl
import dk.zlatan.flotmand.model.service.AccountService
import dk.zlatan.flotmand.model.service.DateVotingService
import dk.zlatan.flotmand.model.service.DinnerEventService
import dk.zlatan.flotmand.model.service.NotificationService
import dk.zlatan.flotmand.model.service.PlacesService
import dk.zlatan.flotmand.model.service.RotationService

@Module
@InstallIn(SingletonComponent::class)
abstract class ServiceModule {
    @Binds
    abstract fun provideAccountService(impl: AccountServiceImpl): AccountService

    @Binds
    abstract fun provideDinnerEvent(impl: DinnerEventServiceImpl): DinnerEventService

    @Binds
    abstract fun providePlacesService(impl: PlacesServiceImpl): PlacesService

    @Binds
    abstract fun provideDateVotingService(impl: DateVotingServiceImpl): DateVotingService

    @Binds
    abstract fun provideNotificationService(impl: NotificationServiceImpl): NotificationService

    @Binds
    abstract fun provideRotationService(impl: RotationServiceImpl): RotationService
}