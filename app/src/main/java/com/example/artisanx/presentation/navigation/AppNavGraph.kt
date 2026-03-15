package com.example.artisanx.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.example.artisanx.presentation.customer.*
import com.example.artisanx.presentation.artisan.*
import com.example.artisanx.presentation.common.JobDetailScreen
import com.example.artisanx.presentation.profile.*

sealed class BottomNavItem(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object CustomerHome : BottomNavItem(Screen.CustomerDashboard.route, "Home", Icons.Default.Home)
    object CustomerProfile : BottomNavItem(Screen.CustomerProfile.route, "Profile", Icons.Default.Person)
    
    object ArtisanHome : BottomNavItem(Screen.ArtisanDashboard.route, "Home", Icons.Default.Home)
    object ArtisanBrowse : BottomNavItem(Screen.JobBrowse.route, "Jobs", Icons.Default.Home)
    object ArtisanProfile : BottomNavItem(Screen.ArtisanProfile.route, "Profile", Icons.Default.Person)
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val isCustomerFlow = currentDestination?.route?.startsWith("customer") == true
    val isArtisanFlow = currentDestination?.route?.startsWith("artisan") == true || currentDestination?.route == Screen.JobBrowse.route

    val showBottomBar = isCustomerFlow || isArtisanFlow

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    val items = if (isCustomerFlow) {
                        listOf(BottomNavItem.CustomerHome, BottomNavItem.CustomerProfile)
                    } else {
                        listOf(BottomNavItem.ArtisanHome, BottomNavItem.ArtisanBrowse, BottomNavItem.ArtisanProfile)
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
            composable(route = Screen.Login.route) {
                LoginScreen(navController = navController, snackbarHostState = snackbarHostState)
            }
            composable(route = Screen.Register.route) {
                RegisterScreen(navController = navController, snackbarHostState = snackbarHostState)
            }
            composable(route = Screen.RoleSelection.route) {
                RoleSelectionScreen(navController = navController)
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

            // Artisan
            composable(route = Screen.ArtisanDashboard.route) {
                ArtisanDashboardScreen()
            }
            composable(route = Screen.JobBrowse.route) {
                JobBrowseScreen(navController)
            }
            composable(route = Screen.ArtisanProfile.route) {
                ArtisanProfileScreen(navController)
            }
            
            // Common
            composable(route = Screen.JobDetail.route) {
                JobDetailScreen(navController = navController, snackbarHostState = snackbarHostState)
            }
        }
    }
}
