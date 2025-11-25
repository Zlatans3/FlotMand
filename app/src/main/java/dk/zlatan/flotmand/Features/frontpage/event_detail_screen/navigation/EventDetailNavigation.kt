package dk.zlatan.flotmand.Features.frontpage.event_detail_screen.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import dk.zlatan.flotmand.Features.frontpage.event_detail_screen.EventDetailScreenRoute

const val EventDetailRoute = "event_detail"
const val EventDetailEventIdArg = "eventId"
val EventDetailRouteWithArgs = "$EventDetailRoute/{$EventDetailEventIdArg}"

fun NavController.navigateToEventDetail(eventId: String) {
    navigate("$EventDetailRoute/$eventId")
}

fun NavGraphBuilder.eventDetailScreen() {
    composable(
        route = EventDetailRouteWithArgs,
        arguments = listOf(navArgument(EventDetailEventIdArg) { type = NavType.StringType })
    ) { backStackEntry ->
        val eventId = backStackEntry.arguments?.getString(EventDetailEventIdArg)
        EventDetailScreenRoute(
            eventId = eventId
        )
    }
}
