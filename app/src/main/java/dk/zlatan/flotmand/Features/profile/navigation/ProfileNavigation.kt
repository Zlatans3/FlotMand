package dk.zlatan.flotmand.Features.profile.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.IntOffset
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import dk.zlatan.flotmand.Features.profile.ProfileScreenRoute
import dk.zlatan.flotmand.Features.profile.account_information.AccountInformationScreenRoute

@Suppress("CyclomaticComplexMethod")
@Composable
fun ProfileNavigation(viewModel: ProfileNavigationViewModel = hiltViewModel()) {
    val navigationStack: List<ProfileDestination> by viewModel.navigationStack.collectAsStateWithLifecycle()

    // Navigation overlay for sub-screens
    NavDisplay(
        backStack = navigationStack,
        onBack = { viewModel.pop() },
        entryProvider = { key ->
            when (key) {
                ProfileDestination.ProfileScreen -> {
                    NavEntry(key) {
                        ProfileScreenRoute(
                            navigateToAccountInformation = {
                                viewModel.navigate(ProfileDestination.AccountInformation)
                            },
                        )
                    }
                }

                ProfileDestination.AccountInformation -> {
                    NavEntry(key) {
                        AccountInformationScreenRoute(
                            onDismiss = { viewModel.pop() },
                        )
                    }
                }
            }
        },
        transitionSpec = {
            ContentTransform(
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                ),
                ExitTransition.None,
            )
        },
        popTransitionSpec = {
            ContentTransform(
                EnterTransition.None,
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                ),
            )
        },
        predictivePopTransitionSpec = { progress: Int ->
            ContentTransform(
                EnterTransition.None,
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec =
                        tween<IntOffset>(
                            durationMillis = (400 * (100 - progress) / 100).coerceAtLeast(1),
                            easing = FastOutSlowInEasing,
                        ),
                ),
            )
        },
        entryDecorators =
            listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
//            rememberViewModelStoreNavEntryDecorator()
            ),
    )
}
