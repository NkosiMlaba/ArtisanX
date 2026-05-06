package com.example.artisanx.presentation.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.RateReview
import com.example.artisanx.presentation.common.LocationPickerScreen
import com.example.artisanx.presentation.common.OnLifecycleResume
import com.example.artisanx.presentation.common.RatingStars
import com.example.artisanx.presentation.navigation.Screen
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    OnLifecycleResume { viewModel.refresh() }
    val user = viewModel.user.value
    val profileDoc = viewModel.profileDoc.value
    val isLoading = viewModel.isLoading.value
    val isEditing = viewModel.isEditing.value

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collectLatest { event ->
            when(event) {
                is ProfileViewModel.UiEvent.Navigate -> {
                    navController.navigate(event.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
                else -> Unit
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Customer Profile") }) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            if (isLoading && user == null) {
                CircularProgressIndicator()
            } else if (user != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isEditing) {
                        OutlinedTextField(
                            value = viewModel.editName.value,
                            onValueChange = { viewModel.editName.value = it },
                            label = { Text("Full Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = user.email,
                            onValueChange = {}, // Email is read-only
                            label = { Text("Email (Locked)") },
                            enabled = false,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        val fullName = profileDoc?.data?.get("fullName") as? String ?: user.name
                        Text("Name: $fullName", style = MaterialTheme.typography.headlineSmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Email: ${user.email}", style = MaterialTheme.typography.bodyLarge)

                        Spacer(modifier = Modifier.height(20.dp))
                        ReviewsNavButton(
                            count = viewModel.customerReviewsGivenCount.value,
                            label = "Reviews Given",
                            emptyLabel = "No reviews yet",
                            onClick = {
                                navController.navigate(
                                    Screen.ReviewsList.createRoute(Screen.ReviewsList.MODE_GIVEN)
                                )
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    if (isEditing) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            OutlinedButton(onClick = { viewModel.cancelEditing() }, enabled = !isLoading) {
                                Text("Cancel")
                            }
                            Button(onClick = { viewModel.saveProfile() }, enabled = !isLoading) {
                                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                                else Text("Save")
                            }
                        }
                    } else {
                        Button(onClick = { viewModel.startEditing() }, enabled = !isLoading, modifier = Modifier.fillMaxWidth()) {
                            Text("Edit Profile")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(onClick = { viewModel.logout() }, enabled = !isLoading) {
                        Text("Logout", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtisanProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    OnLifecycleResume { viewModel.refresh() }
    val user = viewModel.user.value
    val profileDoc = viewModel.profileDoc.value
    val isLoading = viewModel.isLoading.value
    val isEditing = viewModel.isEditing.value
    var showMapPicker by remember { mutableStateOf(false) }

    if (showMapPicker) {
        val initLat = viewModel.editLatitude.value.takeIf { it != 0.0 } ?: -29.8587
        val initLng = viewModel.editLongitude.value.takeIf { it != 0.0 } ?: 31.0218
        LocationPickerScreen(
            initialLocation = LatLng(initLat, initLng),
            onLocationSelected = { result ->
                viewModel.editServiceArea.value = result.address
                viewModel.editLatitude.value = result.latitude
                viewModel.editLongitude.value = result.longitude
                showMapPicker = false
            },
            onDismiss = { showMapPicker = false }
        )
        return
    }

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collectLatest { event ->
            when(event) {
                is ProfileViewModel.UiEvent.Navigate -> {
                    navController.navigate(event.route) {
                        popUpTo(0) { inclusive = true } 
                    }
                }
                else -> Unit
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Artisan Profile") }) }
    ) { padding ->
         Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (isLoading && user == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (user != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isEditing) {
                        OutlinedTextField(
                            value = viewModel.editName.value,
                            onValueChange = { viewModel.editName.value = it },
                            label = { Text("Full Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = viewModel.editPhone.value,
                            onValueChange = { viewModel.editPhone.value = it },
                            label = { Text("Phone Number") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = viewModel.editTradeCategory.value,
                            onValueChange = { viewModel.editTradeCategory.value = it },
                            label = { Text("Trade Category") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = viewModel.editSkills.value,
                            onValueChange = { viewModel.editSkills.value = it },
                            label = { Text("Skills") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = viewModel.editServiceArea.value,
                            onValueChange = { viewModel.editServiceArea.value = it },
                            label = { Text("Service Area") },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = { showMapPicker = true }) {
                                    Icon(Icons.Filled.LocationOn, contentDescription = "Set on map")
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = user.email,
                            onValueChange = {}, // Email is read-only
                            label = { Text("Email (Locked)") },
                            enabled = false,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        val fullName = profileDoc?.data?.get("fullName") as? String ?: user.name
                        val badge = profileDoc?.data?.get("badge") as? String ?: "Artisan"
                        val isVerified = (profileDoc?.data?.get("verified") as? Boolean) == true ||
                                badge.equals("Verified Artisan", ignoreCase = true)
                        val avgRating = viewModel.computedAvgRating.value
                        val reviewCount = viewModel.computedReviewCount.value

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(fullName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            if (isVerified) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Filled.Verified,
                                    contentDescription = "Verified Artisan",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        if (!isVerified) {
                            Text(badge, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        Text("Email: ${user.email}", style = MaterialTheme.typography.bodyMedium)

                        // Star rating display with fractional fill
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RatingStars(rating = avgRating, starSize = 22.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (reviewCount > 0) String.format("%.1f (%d reviews)", avgRating, reviewCount)
                                       else "No reviews yet",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        val phone = profileDoc?.data?.get("phone") as? String ?: "Not set"
                        val trade = profileDoc?.data?.get("tradeCategory") as? String ?: "Not set"
                        val skills = profileDoc?.data?.get("skills") as? String ?: "Not set"
                        val area = profileDoc?.data?.get("serviceArea") as? String ?: "Not set"

                        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Phone: $phone", style = MaterialTheme.typography.bodyMedium)
                                Text("Trade: $trade", style = MaterialTheme.typography.bodyMedium)
                                Text("Skills: $skills", style = MaterialTheme.typography.bodyMedium)
                                Text("Service Area: $area", style = MaterialTheme.typography.bodyMedium)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        ReviewsNavButton(
                            count = reviewCount,
                            label = "Reviews Received",
                            emptyLabel = "No reviews yet",
                            onClick = {
                                navController.navigate(
                                    Screen.ReviewsList.createRoute(Screen.ReviewsList.MODE_RECEIVED)
                                )
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (isEditing) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            OutlinedButton(onClick = { viewModel.cancelEditing() }, enabled = !isLoading) {
                                Text("Cancel")
                            }
                            Button(onClick = { viewModel.saveProfile() }, enabled = !isLoading) {
                                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                                else Text("Save")
                            }
                        }
                    } else {
                        Button(onClick = { viewModel.startEditing() }, enabled = !isLoading, modifier = Modifier.fillMaxWidth()) {
                            Text("Edit Profile")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(onClick = { viewModel.logout() }, enabled = !isLoading) {
                        Text("Logout", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewsNavButton(
    count: Int,
    label: String,
    emptyLabel: String,
    onClick: () -> Unit
) {
    val enabled = count > 0
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .let { if (enabled) it.clickable { onClick() } else it },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.RateReview,
                contentDescription = null,
                tint = if (enabled) MaterialTheme.colorScheme.primary
                       else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (enabled) "$label ($count)" else label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!enabled) {
                    Text(
                        text = emptyLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
            if (enabled) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
