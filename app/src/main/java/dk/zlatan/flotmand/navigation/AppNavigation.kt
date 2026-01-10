package dk.zlatan.flotmand.navigation

import android.view.WindowManager
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import dk.zlatan.flotmand.Features.authentication.di.AuthenticationNavigation
import dk.zlatan.flotmand.Features.bottomnavigation.FmBottomNavigationBar
import dk.zlatan.flotmand.Features.bottomnavigation.TopLevelDestination
import dk.zlatan.flotmand.Features.frontpage.navigation.FrontPageNavigation
import dk.zlatan.flotmand.Features.frontpage.navigation.FrontPageNavigationViewModel
import dk.zlatan.flotmand.Features.my_events.navigaiton.MyEventsNavigation
import dk.zlatan.flotmand.Features.my_events.navigaiton.MyEventsNavigationViewModel
import dk.zlatan.flotmand.Features.profile.navigation.ProfileNavigation
import dk.zlatan.flotmand.Features.profile.navigation.ProfileNavigationViewModel

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    viewModel: AppNavigationViewModel = hiltViewModel()
) {
    val navigationStack: List<AppDestination> by viewModel.navigationStack.collectAsStateWithLifecycle()
    val isCheckingAuth: Boolean by viewModel.isCheckingAuth.collectAsStateWithLifecycle()

    // Keep showing splash screen while checking authentication
    // by not rendering anything until the check is complete
    if (isCheckingAuth) {
        return
    }

    NavDisplay(
        backStack = navigationStack,
        onBack = { /* Don't allow back at app level */ },
        entryProvider = { key ->
            when (key) {
                AppDestination.Authentication -> NavEntry(key) {
                    AuthenticationNavigation()
                }

                AppDestination.MainApp -> NavEntry(key) {
                    MainAppContent(modifier = modifier)
                }
            }
        },
        transitionSpec = {
            ContentTransform(
                fadeIn(),
                fadeOut()
            )
        },
        popTransitionSpec = {
            ContentTransform(
                fadeIn(),
                fadeOut()
            )
        },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator()
        )
    )
}

@Composable
private fun MainAppContent(
    modifier: Modifier = Modifier
) {
    var currentTab: TopLevelDestination by rememberSaveable {
        mutableStateOf(TopLevelDestination.HOME)
    }

    // Get navigation ViewModels to access resetToRoot
    val frontPageViewModel: FrontPageNavigationViewModel = hiltViewModel()
    val myEventsViewModel: MyEventsNavigationViewModel = hiltViewModel()
    val profileViewModel: ProfileNavigationViewModel = hiltViewModel()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            FmBottomNavigationBar(
                currentTab = currentTab,
                selectedTabIconColor = MaterialTheme.colorScheme.primary,
                onBottomNavigationClicked = { destination ->
                    // If clicking the same tab, reset to root
                    if (currentTab == destination) {
                        when (destination) {
                            TopLevelDestination.HOME -> frontPageViewModel.resetToRoot()
                            TopLevelDestination.MY_EVENTS -> myEventsViewModel.resetToRoot()
                            TopLevelDestination.PROFILE -> profileViewModel.resetToRoot()
                        }
                    } else {
                        currentTab = destination
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                TopLevelDestination.HOME -> {
                    FrontPageNavigation(viewModel = frontPageViewModel)
                }
                TopLevelDestination.MY_EVENTS -> {
                    MyEventsNavigation(viewModel = myEventsViewModel)
                }
                TopLevelDestination.PROFILE -> {
                    ProfileNavigation(viewModel = profileViewModel)
                }
            }
        }
    }

    // Keep screen on for HOME and PROFILE tabs
    val window = LocalActivity.current?.window
    LaunchedEffect(currentTab) {
        when (currentTab) {
            TopLevelDestination.HOME,
            TopLevelDestination.PROFILE -> {
                window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
            else -> window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
}

