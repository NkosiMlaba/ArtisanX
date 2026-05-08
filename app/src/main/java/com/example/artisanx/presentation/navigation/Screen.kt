package com.example.artisanx.presentation.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object RoleSelection : Screen("role_selection")

    // Customer Screens
    object CustomerDashboard : Screen("customer_dashboard")
    object PostJob : Screen("post_job")
    object EditJob : Screen("edit_job/{jobId}") {
        fun createRoute(jobId: String) = "edit_job/$jobId"
    }
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

    // Chat
    object Chat : Screen("chat/{bookingId}") {
        fun createRoute(bookingId: String) = "chat/$bookingId"
    }

    // Review
    object Review : Screen("review/{bookingId}/{artisanId}") {
        fun createRoute(bookingId: String, artisanId: String) = "review/$bookingId/$artisanId"
    }

    // Reviews list (mode = "received" for artisans, "given" for customers)
    object ReviewsList : Screen("reviews_list/{mode}") {
        fun createRoute(mode: String) = "reviews_list/$mode"
        const val MODE_RECEIVED = "received"
        const val MODE_GIVEN = "given"
    }

    // Single review detail
    object ReviewDetail : Screen("review_detail/{reviewId}") {
        fun createRoute(reviewId: String) = "review_detail/$reviewId"
    }

    // Credits
    object BuyCredits : Screen("buy_credits")

    // Legal
    object PrivacyPolicy : Screen("privacy_policy")

    // Chat list (role-prefixed to keep bottom nav detection consistent)
    object CustomerChatList : Screen("customer_chat_list")
    object ArtisanChatList : Screen("artisan_chat_list")
}
