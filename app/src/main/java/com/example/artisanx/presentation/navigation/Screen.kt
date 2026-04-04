package com.example.artisanx.presentation.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object RoleSelection : Screen("role_selection")

    // Customer Screens
    object CustomerDashboard : Screen("customer_dashboard")
    object PostJob : Screen("post_job")
    object CustomerProfile : Screen("customer_profile")
    object CustomerBookings : Screen("customer_bookings")

    // Artisan Screens
    object ArtisanDashboard : Screen("artisan_dashboard")
    object JobBrowse : Screen("job_browse")
    object ArtisanProfile : Screen("artisan_profile")
    object ArtisanOnboarding : Screen("artisan_onboarding")
    object ArtisanBookings : Screen("artisan_bookings")

    // Common
    object JobDetail : Screen("job_detail/{jobId}") {
        fun createRoute(jobId: String) = "job_detail/$jobId"
    }

    // Bidding
    object BidSubmit : Screen("bid_submit/{jobId}") {
        fun createRoute(jobId: String) = "bid_submit/$jobId"
    }
    object BidsList : Screen("bids_list/{jobId}") {
        fun createRoute(jobId: String) = "bids_list/$jobId"
    }
}
