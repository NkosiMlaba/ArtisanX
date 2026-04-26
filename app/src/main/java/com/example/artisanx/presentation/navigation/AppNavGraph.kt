package com.example.artisanx.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.artisanx.presentation.auth.LoginScreen
import com.example.artisanx.presentation.auth.RegisterScreen
import com.example.artisanx.presentation.onboarding.RoleSelectionScreen
import com.example.artisanx.presentation.onboarding.ArtisanOnboardingScreen
import com.example.artisanx.presentation.customer.*
import com.example.artisanx.presentation.artisan.*
import com.example.artisanx.presentation.bidding.BidSubmitScreen
import com.example.artisanx.presentation.bidding.BidsListScreen
import com.example.artisanx.presentation.booking.BookingsScreen
import com.example.artisanx.presentation.chat.ChatListScreen
import com.example.artisanx.presentation.chat.ChatScreen
import com.example.artisanx.presentation.common.JobDetailScreen
import com.example.artisanx.presentation.credits.BuyCreditsScreen
import com.example.artisanx.presentation.profile.*
import com.example.artisanx.presentation.review.ReviewScreen

sealed class BottomNavItem(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object CustomerHome : BottomNavItem(Screen.CustomerDashboard.route, "Home", Icons.Default.Home)
    object CustomerMessages : BottomNavItem(Screen.CustomerChatList.route, "Messages", Icons.AutoMirrored.Filled.Message)
    object CustomerBookings : BottomNavItem(Screen.CustomerBookings.route, "Bookings", Icons.Default.DateRange)
    object CustomerProfile : BottomNavItem(Screen.CustomerProfile.route, "Profile", Icons.Default.Person)

    object ArtisanHome : BottomNavItem(Screen.ArtisanDashboard.route, "Home", Icons.Default.Home)
    object ArtisanBrowse : BottomNavItem(Screen.JobBrowse.route, "Jobs", Icons.Default.Search)
    object ArtisanMessages : BottomNavItem(Screen.ArtisanChatList.route, "Messages", Icons.AutoMirrored.Filled.Message)
    object ArtisanBookings : BottomNavItem(Screen.ArtisanBookings.route, "Bookings", Icons.Default.DateRange)
    object ArtisanProfile : BottomNavItem(Screen.ArtisanProfile.route, "Profile", Icons.Default.Person)
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String,
    pendingBookingId: String? = null,
    onDeepLinkConsumed: () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    // Navigate to chat screen when a notification is tapped
    LaunchedEffect(pendingBookingId) {
        if (!pendingBookingId.isNullOrBlank()) {
            navController.navigate(Screen.Chat.createRoute(pendingBookingId)) {
                launchSingleTop = true
            }
            onDeepLinkConsumed()
        }
    }
    val currentDestination = navBackStackEntry?.destination

    val isCustomerFlow = currentDestination?.route?.startsWith("customer") == true
    val isArtisanFlow = currentDestination?.route?.startsWith("artisan") == true ||
            currentDestination?.route == Screen.JobBrowse.route

    val showBottomBar = isCustomerFlow || isArtisanFlow

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    val items = if (isCustomerFlow) {
                        listOf(
                            BottomNavItem.CustomerHome,
                            BottomNavItem.CustomerMessages,
                            BottomNavItem.CustomerBookings,
                            BottomNavItem.CustomerProfile
                        )
                    } else {
                        listOf(
                            BottomNavItem.ArtisanHome,
                            BottomNavItem.ArtisanBrowse,
                            BottomNavItem.ArtisanMessages,
                            BottomNavItem.ArtisanBookings,
                            BottomNavItem.ArtisanProfile
                        )
                    }
                    items.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.title) },
                            label = { Text(item.title) },
                            selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Auth
            composable(route = Screen.Login.route) {
                LoginScreen(navController = navController, snackbarHostState = snackbarHostState)
            }
            composable(route = Screen.Register.route) {
                RegisterScreen(navController = navController, snackbarHostState = snackbarHostState)
            }
            composable(route = Screen.RoleSelection.route) {
                RoleSelectionScreen(navController = navController)
            }
            composable(route = Screen.ArtisanOnboarding.route) {
                ArtisanOnboardingScreen(navController = navController, snackbarHostState = snackbarHostState)
            }

            // Customer
            composable(route = Screen.CustomerDashboard.route) {
                CustomerDashboardScreen(navController)
            }
            composable(route = Screen.PostJob.route) {
                PostJobScreen(navController = navController, snackbarHostState = snackbarHostState)
            }
            composable(route = Screen.CustomerProfile.route) {
                CustomerProfileScreen(navController)
            }
            composable(route = Screen.CustomerBookings.route) {
                BookingsScreen(isArtisan = false, navController = navController, snackbarHostState = snackbarHostState)
            }

            // Artisan
            composable(route = Screen.ArtisanDashboard.route) {
                ArtisanDashboardScreen(navController = navController)
            }
            composable(route = Screen.JobBrowse.route) {
                JobBrowseScreen(navController)
            }
            composable(route = Screen.ArtisanProfile.route) {
                ArtisanProfileScreen(navController)
            }
            composable(route = Screen.ArtisanBookings.route) {
                BookingsScreen(isArtisan = true, navController = navController, snackbarHostState = snackbarHostState)
            }

            // Common
            composable(route = Screen.JobDetail.route) {
                JobDetailScreen(navController = navController, snackbarHostState = snackbarHostState)
            }

            // Bidding
            composable(route = Screen.BidSubmit.route) {
                BidSubmitScreen(navController = navController, snackbarHostState = snackbarHostState)
            }
            composable(route = Screen.BidsList.route) {
                BidsListScreen(navController = navController, snackbarHostState = snackbarHostState)
            }

            // Chat list
            composable(route = Screen.CustomerChatList.route) {
                ChatListScreen(navController = navController)
            }
            composable(route = Screen.ArtisanChatList.route) {
                ChatListScreen(navController = navController)
            }

            // Chat
            composable(route = Screen.Chat.route) {
                ChatScreen(navController = navController, snackbarHostState = snackbarHostState)
            }

            // Review
            composable(route = Screen.Review.route) {
                ReviewScreen(navController = navController, snackbarHostState = snackbarHostState)
            }

            // Buy Credits
            composable(route = Screen.BuyCredits.route) {
                BuyCreditsScreen(navController = navController, snackbarHostState = snackbarHostState)
            }
        }
    }
}
