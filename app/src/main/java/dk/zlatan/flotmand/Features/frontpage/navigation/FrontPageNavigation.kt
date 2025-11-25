package dk.zlatan.flotmand.Features.frontpage.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import dk.zlatan.flotmand.Features.frontpage.FrontPageRoute
import dk.zlatan.flotmand.Features.frontpage.event_detail_screen.navigation.eventDetailScreen
import dk.zlatan.flotmand.Features.frontpage.event_detail_screen.navigation.navigateToEventDetail

object FrontPageDestination {
    const val route = "front_page"
    const val eventIdArg = "eventId"
    val routeWithArgs = "$route/{$eventIdArg}"
}

fun NavController.navigateToFrontPage(builder: NavOptionsBuilder.() -> Unit = {}) {
    navigate("front_page_graph", builder)
}

fun NavGraphBuilder.frontPageSection(
    onEventClicked: (String) -> Unit,
) {
    navigation(
        startDestination = FrontPageDestination.routeWithArgs,
        route = "front_page_graph"
    ) {
        composable(
            route = FrontPageDestination.routeWithArgs,
            arguments = listOf(navArgument(FrontPageDestination.eventIdArg) { type = NavType.StringType })
        ) { backStackEntry ->
            FrontPageRoute(
                onClickEvent = onEventClicked,
            )
        }
        eventDetailScreen()
    }
}
