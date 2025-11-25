package dk.zlatan.flotmand.Features.my_events.navigaiton

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import dk.zlatan.flotmand.Features.my_events.MyEventScreen
import kotlinx.serialization.Serializable

@Serializable data object MyEventsDestinationRoute

const val MyEventsGraphRoute = "my_events"

fun NavController.navigateToMyEvents(builder: NavOptionsBuilder.() -> Unit = {}) {
    navigate(MyEventsGraphRoute, builder)
}

fun NavGraphBuilder.myEventScreen(
) {
    navigation(startDestination = MyEventsDestinationRoute.toString(), route = MyEventsGraphRoute) {
        composable(
            route = MyEventsDestinationRoute.toString()
        ) {
            MyEventScreen()
        }
    }
}