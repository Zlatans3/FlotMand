package dk.zlatan.flotmand.navigation

sealed class AppDestination {
    data object Authentication : AppDestination()

    data object ProfileSetup : AppDestination()

    data object MainApp : AppDestination()
}
