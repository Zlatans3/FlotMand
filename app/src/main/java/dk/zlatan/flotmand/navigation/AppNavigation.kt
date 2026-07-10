package dk.zlatan.flotmand.navigation

import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import dk.zlatan.flotmand.R
import kotlinx.coroutines.launch
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import dk.zlatan.flotmand.Features.authentication.di.AuthenticationNavigation
import dk.zlatan.flotmand.Features.bottomnavigation.FmBottomNavigationBar
import dk.zlatan.flotmand.Features.bottomnavigation.TopLevelDestination
import dk.zlatan.flotmand.Features.frontpage.navigation.FrontPageDestination
import dk.zlatan.flotmand.Features.frontpage.navigation.FrontPageNavigation
import dk.zlatan.flotmand.Features.frontpage.navigation.FrontPageNavigationViewModel
import dk.zlatan.flotmand.Features.my_events.navigaiton.MyEventsDestination
import dk.zlatan.flotmand.Features.my_events.navigaiton.MyEventsNavigation
import dk.zlatan.flotmand.Features.my_events.navigaiton.MyEventsNavigationViewModel
import dk.zlatan.flotmand.Features.onboarding.ProfileSetupScreenRoute
import dk.zlatan.flotmand.Features.polls.navigation.PollsNavigation
import dk.zlatan.flotmand.Features.polls.navigation.PollsNavigationViewModel
import dk.zlatan.flotmand.Features.profile.navigation.ProfileDestination
import dk.zlatan.flotmand.Features.profile.navigation.ProfileNavigation
import dk.zlatan.flotmand.Features.profile.navigation.ProfileNavigationViewModel
import dk.zlatan.flotmand.design_system.componenets.NotificationPermissionHandler

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    viewModel: AppNavigationViewModel = hiltViewModel(),
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
                AppDestination.Authentication -> {
                    NavEntry(key) {
                        AuthenticationNavigation()
                    }
                }

                AppDestination.ProfileSetup -> {
                    NavEntry(key) {
                        ProfileSetupScreenRoute(modifier = modifier)
                    }
                }

                AppDestination.MainApp -> {
                    NavEntry(key) {
                        MainAppContent(modifier = modifier)
                    }
                }
            }
        },
        transitionSpec = {
            ContentTransform(
                fadeIn(),
                fadeOut(),
            )
        },
        popTransitionSpec = {
            ContentTransform(
                fadeIn(),
                fadeOut(),
            )
        },
        entryDecorators =
            listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
    )
}

@Composable
private fun MainAppContent(modifier: Modifier = Modifier) {
    NotificationPermissionHandler()

    var currentTab: TopLevelDestination by rememberSaveable {
        mutableStateOf(TopLevelDestination.HOME)
    }

    // Get navigation ViewModels to access resetToRoot
    val frontPageViewModel: FrontPageNavigationViewModel = hiltViewModel()
    val myEventsViewModel: MyEventsNavigationViewModel = hiltViewModel()
    val profileViewModel: ProfileNavigationViewModel = hiltViewModel()
    val pollsViewModel: PollsNavigationViewModel = hiltViewModel()

    val frontPageStack by frontPageViewModel.navigationStack.collectAsStateWithLifecycle()
    val pollsStack by pollsViewModel.navigationStack.collectAsStateWithLifecycle()
    val myEventsStack by myEventsViewModel.navigationStack.collectAsStateWithLifecycle()
    val profileStack by profileViewModel.navigationStack.collectAsStateWithLifecycle()

    val isAtRoot = when (currentTab) {
        TopLevelDestination.HOME -> frontPageStack.size <= 1
        TopLevelDestination.POLLS -> pollsStack.size <= 1
        TopLevelDestination.MY_EVENTS -> myEventsStack.size <= 1
        TopLevelDestination.PROFILE -> profileStack.size <= 1
    }

    val snackbarHostState = remember { SnackbarHostState() }
    var canExit by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val activity = LocalActivity.current
    val exitMessage = stringResource(R.string.press_back_again_to_exit)

    BackHandler(enabled = isAtRoot) {
        if (canExit) {
            activity?.finish()
        } else {
            canExit = true
            scope.launch {
                snackbarHostState.showSnackbar(exitMessage)
                canExit = false
            }
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                            TopLevelDestination.POLLS -> pollsViewModel.resetToRoot()
                        }
                    } else {
                        currentTab = destination
                    }
                },
            )
        },
    ) { innerPadding ->
        val bottomPaddingValue = innerPadding.calculateBottomPadding()
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(bottom = bottomPaddingValue),
        ) {
            when (currentTab) {
                TopLevelDestination.HOME -> {
                    FrontPageNavigation(
                        viewModel = frontPageViewModel,
                        onEventCreated = { eventId ->
                            currentTab = TopLevelDestination.MY_EVENTS
                            myEventsViewModel.navigate(MyEventsDestination.EventDetail(eventId))
                        },
                        onNavigateToAccountInformation = {
                            currentTab = TopLevelDestination.PROFILE
                            profileViewModel.navigate(ProfileDestination.AccountInformation)
                        },
                        onNavigateToPolls = {
                            currentTab = TopLevelDestination.POLLS
                        },
                    )
                }

                TopLevelDestination.MY_EVENTS -> {
                    MyEventsNavigation(
                        viewModel = myEventsViewModel,
                        onNavigateToAccountInformation = {
                            currentTab = TopLevelDestination.PROFILE
                            profileViewModel.navigate(ProfileDestination.AccountInformation)
                        },
                    )
                }

                TopLevelDestination.PROFILE -> {
                    ProfileNavigation(viewModel = profileViewModel)
                }

                TopLevelDestination.POLLS -> {
                    PollsNavigation(
                        viewModel = pollsViewModel,
                        onEventCreated = { eventId ->
                            currentTab = TopLevelDestination.MY_EVENTS
                            myEventsViewModel.navigate(MyEventsDestination.EventDetail(eventId))
                        },
                    )
                }
            }
        }
    }

    // Switch to HOME tab when the coordinator pushes Notifications (e.g. from a system notification tap)
    LaunchedEffect(frontPageStack) {
        if (frontPageStack.contains(FrontPageDestination.Notifications) &&
            currentTab != TopLevelDestination.HOME
        ) {
            currentTab = TopLevelDestination.HOME
        }
    }

    // Keep screen on for HOME and PROFILE tabs
    val window = LocalActivity.current?.window
    LaunchedEffect(currentTab) {
        when (currentTab) {
            TopLevelDestination.HOME,
            TopLevelDestination.PROFILE,
            -> {
                window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }

            else -> {
                window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }
}
