package dk.zlatan.flotmand.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import dk.zlatan.flotmand.R
import dk.zlatan.flotmand.design_system.icon.FmIcons

enum class TopLevelDestination(
    val unselectedIcon: ImageVector,
    @StringRes val iconTextId: Int,
    val route: String,
) {
    HOME(
        unselectedIcon = FmIcons.Home,
        iconTextId = R.string.home_title,
        route = "front_page_graph",
    ),
    MY_EVENTS(
        unselectedIcon = FmIcons.Calendar,
        iconTextId = R.string.my_event_title,
        route = "my_events",
    ),
    PROFILE(
        unselectedIcon = FmIcons.Person,
        iconTextId = R.string.profile_title,
        route = "profile",
    ),
}
