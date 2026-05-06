package com.example.artisanx

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.lifecycleScope
import com.example.artisanx.notifications.BidNotificationManager
import com.example.artisanx.notifications.BookingNotificationManager
import com.example.artisanx.notifications.ChatNotificationManager
import com.example.artisanx.presentation.common.OfflineBanner
import com.example.artisanx.presentation.navigation.AppNavGraph
import com.example.artisanx.ui.theme.ArtisanXTheme
import com.example.artisanx.util.SessionEventBus
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface BidNotifEntryPoint {
        fun bidNotificationManager(): BidNotificationManager
        fun chatNotificationManager(): ChatNotificationManager
        fun bookingNotificationManager(): BookingNotificationManager
    }

    private lateinit var bidNotificationManager: BidNotificationManager
    private lateinit var chatNotificationManager: ChatNotificationManager
    private lateinit var bookingNotificationManager: BookingNotificationManager
    private var isOnline by mutableStateOf(true)
    private lateinit var connectivityManager: ConnectivityManager
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* no-op: app continues either way */ }
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) { isOnline = true }
        override fun onLost(network: Network) { isOnline = false }
        override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
            isOnline = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleDeepLinkIntent(intent)

        connectivityManager = getSystemService(ConnectivityManager::class.java)
        connectivityManager.registerNetworkCallback(
            NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build(),
            networkCallback
        )
        isOnline = connectivityManager.activeNetwork
            ?.let { connectivityManager.getNetworkCapabilities(it) }
            ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false

        val notifEntry = EntryPointAccessors
            .fromApplication(applicationContext, BidNotifEntryPoint::class.java)
        bidNotificationManager = notifEntry.bidNotificationManager()
        chatNotificationManager = notifEntry.chatNotificationManager()
        bookingNotificationManager = notifEntry.bookingNotificationManager()
        bidNotificationManager.start(lifecycleScope)
        chatNotificationManager.start(lifecycleScope)
        bookingNotificationManager.start(lifecycleScope)

        // Restart subscriptions whenever the session changes (login / logout / account switch)
        lifecycleScope.launch {
            SessionEventBus.sessionChanged.collect {
                bidNotificationManager.start(lifecycleScope)
                chatNotificationManager.start(lifecycleScope)
                bookingNotificationManager.start(lifecycleScope)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            ArtisanXTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (viewModel.isLoading.value) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.artisanx_logo_bg),
                                    contentDescription = "ArtisanX",
                                    modifier = Modifier
                                        .size(120.dp)
                                        .clip(RoundedCornerShape(percent = 30)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                CircularProgressIndicator()
                            }
                        }
                    } else {
                        val navController = rememberNavController()
                        androidx.compose.runtime.LaunchedEffect(Unit) {
                            SessionEventBus.sessionExpired.collect {
                                navController.navigate("login") {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        }
                        Column(modifier = Modifier.fillMaxSize()) {
                            if (!isOnline) OfflineBanner()
                            AppNavGraph(
                                navController = navController,
                                startDestination = viewModel.startDestination.value,
                                pendingBookingId = viewModel.pendingDeepLinkBookingId.value,
                                pendingJobId = viewModel.pendingDeepLinkJobId.value,
                                onDeepLinkConsumed = viewModel::clearDeepLink
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::bidNotificationManager.isInitialized) bidNotificationManager.start(lifecycleScope)
        if (::chatNotificationManager.isInitialized) chatNotificationManager.start(lifecycleScope)
        if (::bookingNotificationManager.isInitialized) bookingNotificationManager.start(lifecycleScope)
    }

    override fun onDestroy() {
        super.onDestroy()
        connectivityManager.unregisterNetworkCallback(networkCallback)
        if (::bidNotificationManager.isInitialized) bidNotificationManager.stop()
        if (::chatNotificationManager.isInitialized) chatNotificationManager.stop()
        if (::bookingNotificationManager.isInitialized) bookingNotificationManager.stop()
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
        val jobId = intent.getStringExtra(ArtisansXFirebaseService.EXTRA_JOB_ID)
        if (!jobId.isNullOrBlank()) {
            viewModel.setJobDeepLink(jobId)
        }
    }
}