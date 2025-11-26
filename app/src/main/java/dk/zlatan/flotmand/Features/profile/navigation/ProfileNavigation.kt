package dk.zlatan.flotmand.Features.profile.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import dk.zlatan.flotmand.Features.profile.ProfileScreenRoute
import kotlinx.serialization.Serializable

@Serializable data object ProfileRoute
const val ProfileGraphRoute = "profile_graph"

fun NavHostController.navigateToProfile(builder: NavOptionsBuilder.() -> Unit = {}) {
    navigate(ProfileGraphRoute, builder)
}

fun NavGraphBuilder.profileScreen(onNavigateToLogin: () -> Unit) {
    navigation(startDestination = ProfileRoute.toString(), route = ProfileGraphRoute) {
        composable(ProfileRoute.toString()) {
            ProfileScreenRoute(navigateToLogin = onNavigateToLogin)
        }
    }
}
