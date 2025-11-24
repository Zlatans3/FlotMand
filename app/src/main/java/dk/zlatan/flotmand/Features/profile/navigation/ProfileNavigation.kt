package dk.zlatan.flotmand.Features.profile.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import dk.zlatan.flotmand.Features.profile.ProfileScreenRoute

object ProfileNavigation {
    const val PROFILE_ROUTE = "profile"

    fun NavGraphBuilder.profileScreen(
        onNavigateBack: () -> Unit = {},
        onSignOut: () -> Unit = {}
    ) {
        composable(route = PROFILE_ROUTE) {
            ProfileScreenRoute()
        }
    }

    fun NavHostController.navigateToProfile(navOptions: NavOptions? = null) {
        this.navigate(PROFILE_ROUTE, navOptions)
    }
}
