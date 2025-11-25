package dk.zlatan.flotmand.Features.frontpage.dinner_event.navigaiton

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import dk.zlatan.flotmand.Features.frontpage.dinner_event.MyEventScreen
import dk.zlatan.flotmand.Features.frontpage.dinner_event.MyEventViewModel

object MyEventsDestination {
    const val route = "my_events"
}

fun NavController.navigateToMyEvents(builder: NavOptionsBuilder.() -> Unit = {}) {
    navigate(MyEventsDestination.route, builder)
}

fun NavGraphBuilder.myEventScreen(
    showBackButton: Boolean,
    onBackClick: () -> Unit,
    onMyEventClick: (String) -> Unit,
) {
    composable(
        route = MyEventsDestination.route
    ) {
        MyEventScreen(
            showBackButton = showBackButton,
            onBackClick = onBackClick,
            onTopicClick = onMyEventClick,
        )
    }
}