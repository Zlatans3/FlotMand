package dk.zlatan.flotmand.Features.my_events.navigaiton

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dk.zlatan.flotmand.Features.my_events.add_new_event.AddEventScreenRoute
import kotlinx.serialization.Serializable

@Serializable
data object AddEventRoute

const val AddEventRouteString = "add_event"

fun NavController.navigateToAddEvent() {
    navigate(AddEventRouteString)
}

fun NavGraphBuilder.addEventScreen(
    onDismiss: () -> Unit
) {
    composable(route = AddEventRouteString) {
        AddEventScreenRoute(
            onDismiss = onDismiss
        )
    }
}
