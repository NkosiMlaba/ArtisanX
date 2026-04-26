package com.example.artisanx

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.artisanx.presentation.navigation.AppNavGraph
import com.example.artisanx.ui.theme.ArtisanXTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleDeepLinkIntent(intent)

        setContent {
            ArtisanXTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (viewModel.isLoading.value) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else {
                        val navController = rememberNavController()
                        AppNavGraph(
                            navController = navController,
                            startDestination = viewModel.startDestination.value,
                            pendingBookingId = viewModel.pendingDeepLinkBookingId.value,
                            onDeepLinkConsumed = viewModel::clearDeepLink
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLinkIntent(intent)
    }

    private fun handleDeepLinkIntent(intent: Intent) {
        val bookingId = intent.getStringExtra(ArtisansXFirebaseService.EXTRA_BOOKING_ID)
        if (!bookingId.isNullOrBlank()) {
            viewModel.setDeepLink(bookingId)
        }
    }
}