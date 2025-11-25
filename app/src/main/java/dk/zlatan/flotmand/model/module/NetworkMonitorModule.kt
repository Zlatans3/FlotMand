package dk.zlatan.flotmand.model.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dk.zlatan.flotmand.util.AlwaysOnlineNetworkMonitor
import dk.zlatan.flotmand.util.NetworkMonitor
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkMonitorModule {
    @Binds
    @Singleton
    abstract fun bindNetworkMonitor(
        defaultNetworkMonitor: AlwaysOnlineNetworkMonitor
    ): NetworkMonitor
}