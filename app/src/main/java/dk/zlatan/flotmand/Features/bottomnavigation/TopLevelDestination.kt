package dk.zlatan.flotmand.Features.bottomnavigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import dk.zlatan.flotmand.R
import dk.zlatan.flotmand.design_system.icon.FmIcons

enum class TopLevelDestination(
    val unselectedIcon: ImageVector,
    @StringRes val iconTextId: Int,
) {
    HOME(
        unselectedIcon = FmIcons.Home,
        iconTextId = R.string.home_title,
    ),
    POLLS(
        unselectedIcon = FmIcons.BarChart,
        iconTextId = R.string.polls_title,
    ),
    MY_EVENTS(
        unselectedIcon = FmIcons.Calendar,
        iconTextId = R.string.my_event_title,
    ),
    PROFILE(
        unselectedIcon = FmIcons.Person,
        iconTextId = R.string.profile_title,
    ),
}