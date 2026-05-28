package dk.zlatan.flotmand.Features.profile.navigation

sealed class ProfileDestination {
    object ProfileScreen : ProfileDestination()
    object AccountInformation : ProfileDestination()
    object SwitchLanguage : ProfileDestination()
    object NotificationSettings : ProfileDestination()
}
