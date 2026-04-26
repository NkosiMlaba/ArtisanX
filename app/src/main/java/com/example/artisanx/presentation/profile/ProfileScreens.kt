package com.example.artisanx.presentation.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
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
    val user = viewModel.user.value
    val profileDoc = viewModel.profileDoc.value
    val isLoading = viewModel.isLoading.value
    val isEditing = viewModel.isEditing.value
    val reviews = viewModel.reviews.value

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
                        val badge = profileDoc?.data?.get("badge") as? String ?: "Artisan"
                        val avgRating = when (val r = profileDoc?.data?.get("avgRating")) {
                            is Double -> r; is Float -> r.toDouble(); is Int -> r.toDouble(); else -> 0.0
                        }
                        val reviewCount = when (val c = profileDoc?.data?.get("reviewCount")) {
                            is Int -> c; is Long -> c.toInt(); else -> 0
                        }

                        Text(fullName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(badge, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Email: ${user.email}", style = MaterialTheme.typography.bodyMedium)

                        // Star rating display
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            (1..5).forEach { star ->
                                Icon(
                                    imageVector = if (star <= avgRating.toInt()) Icons.Filled.Star else Icons.Outlined.Star,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
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

                        // Reviews list
                        if (reviews.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Customer Reviews", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            reviews.forEach { review ->
                                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            (1..5).forEach { star ->
                                                Icon(
                                                    imageVector = if (star <= review.rating) Icons.Filled.Star else Icons.Outlined.Star,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = review.createdAt.take(10),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(review.comment, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
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
