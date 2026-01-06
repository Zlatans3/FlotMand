package dk.zlatan.flotmand.Features.profile.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation

const val ProfileGraphRoute = "profile_graph"

fun NavController.navigateToProfile(navOptions: NavOptions? = null) {
    navigate(ProfileGraphRoute, navOptions)
}

fun NavController.navigateToProfile(builder: NavOptionsBuilder.() -> Unit) {
    navigate(ProfileGraphRoute, builder)
}

fun NavGraphBuilder.profileGraph(
    nestedGraphs: NavGraphBuilder.() -> Unit = {}
) {
    navigation(
        route = ProfileGraphRoute,
        startDestination = "profile"
    ) {
        composable("profile") {
            ProfileNavigation()
        }
        nestedGraphs()
    }
}
