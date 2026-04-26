package com.example.artisanx.presentation.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy Policy") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Last updated: April 2025", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

            PolicySection(
                title = "1. Information We Collect",
                body = "We collect information you provide when registering (name, email address), " +
                        "information about jobs you post or bid on, location data you share when " +
                        "setting a job location or service area, and profile photos uploaded to the app."
            )

            PolicySection(
                title = "2. How We Use Your Information",
                body = "Your information is used to provide and improve the ArtisanX marketplace, " +
                        "match customers with artisans, process bookings and payments, send " +
                        "notifications relevant to your activity, and prevent fraud."
            )

            PolicySection(
                title = "3. Data Sharing",
                body = "We do not sell your personal information. We share limited data with service " +
                        "providers (Appwrite for backend services, Google Maps for location features) " +
                        "solely to operate the platform. Artisan profiles are visible to customers " +
                        "searching for services."
            )

            PolicySection(
                title = "4. Location Data",
                body = "Location is only collected when you explicitly set a job address or service area. " +
                        "We do not track your device location in the background."
            )

            PolicySection(
                title = "5. Data Retention",
                body = "Your account data is retained as long as your account is active. You may request " +
                        "deletion of your account and associated data by contacting support."
            )

            PolicySection(
                title = "6. Security",
                body = "We use industry-standard security practices to protect your data. All data is " +
                        "transmitted over HTTPS and stored securely with Appwrite Cloud."
            )

            PolicySection(
                title = "7. Contact Us",
                body = "For privacy-related questions or data deletion requests, contact us at " +
                        "support@artisanx.co.za."
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PolicySection(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleSmall)
        Text(text = body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
