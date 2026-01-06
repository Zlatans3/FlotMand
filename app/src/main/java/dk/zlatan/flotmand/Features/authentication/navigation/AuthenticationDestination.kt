package dk.zlatan.flotmand.Features.authentication.navigation

sealed class AuthenticationDestination {
    data object Login : AuthenticationDestination()
}
