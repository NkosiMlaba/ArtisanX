package com.example.artisanx.presentation.common

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import coil.compose.AsyncImage
import com.example.artisanx.util.AppwriteFileUtils
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.artisanx.presentation.navigation.Screen
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobDetailScreen(
    navController: NavController,
    viewModel: JobDetailViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState
) {
    val job = viewModel.job.value
    val isLoading = viewModel.isLoading.value
    val error = viewModel.error.value
    val userRole = viewModel.userRole.value
    val isOwnJob = viewModel.isOwnJob.value
    val hasAlreadyBid = viewModel.hasAlreadyBid.value
    val bidCount = viewModel.bidCount.value
    val suggestedArtisans = viewModel.suggestedArtisans.value
    val isMatchLoading = viewModel.isMatchLoading.value

    var showDeleteDialog by remember { mutableStateOf(false) }
    var fullscreenImageUrl by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is JobDetailViewModel.UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is JobDetailViewModel.UiEvent.NavigateBack -> {
                    navController.popBackStack()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Job Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isOwnJob && job?.status == "open") {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Job", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (error != null) {
                Text(text = error, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
            } else if (job != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(text = job.title, style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Info chips row
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SuggestionChip(onClick = {}, label = { Text(job.category) })
                        SuggestionChip(onClick = {}, label = { Text(job.status.replaceFirstChar { it.uppercase() }) })
                        if (job.urgency == "urgent") {
                            SuggestionChip(
                                onClick = {},
                                label = { Text("Urgent") },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    labelColor = MaterialTheme.colorScheme.onErrorContainer
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (job.budget > 0) {
                        Text(text = "Budget: R${job.budget}", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (job.address.isNotBlank()) {
                        Text(text = "Location: ${job.address}", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Show map with pin if lat/lng are set
                    if (job.latitude != 0.0 || job.longitude != 0.0) {
                        val jobLocation = LatLng(job.latitude, job.longitude)
                        val cameraState = rememberCameraPositionState {
                            position = CameraPosition.fromLatLngZoom(jobLocation, 14f)
                        }
                        GoogleMap(
                            modifier = Modifier.fillMaxWidth().height(180.dp),
                            cameraPositionState = cameraState,
                            uiSettings = com.google.maps.android.compose.MapUiSettings(
                                zoomControlsEnabled = false,
                                scrollGesturesEnabled = false,
                                zoomGesturesEnabled = false,
                                tiltGesturesEnabled = false,
                                rotationGesturesEnabled = false
                            )
                        ) {
                            Marker(
                                state = MarkerState(position = jobLocation),
                                title = job.title
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        // Navigate button — opens Google Maps directions to job site
                        OutlinedButton(
                            onClick = {
                                val uri = Uri.parse(
                                    "geo:${job.latitude},${job.longitude}?q=${job.latitude},${job.longitude}(${Uri.encode(job.title)})"
                                )
                                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Default.Navigation,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Navigate to Job Site")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Text(text = "Job Type: ${job.jobType.replaceFirstChar { it.uppercase() }}", style = MaterialTheme.typography.bodyMedium)

                    // Job photos
                    if (job.photoIds.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Photos", style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            job.photoIds.forEach { fileId ->
                                val url = AppwriteFileUtils.fileViewUrl(fileId)
                                AsyncImage(
                                    model = url,
                                    contentDescription = "Job photo",
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(MaterialTheme.shapes.small)
                                        .clickable { fullscreenImageUrl = url },
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(text = "Description", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = job.description, style = MaterialTheme.typography.bodyLarge)

                    Spacer(modifier = Modifier.height(24.dp))

                    // Action buttons — artisan view
                    if (userRole == "artisan") {
                        when {
                            job.status == "open" && !hasAlreadyBid -> {
                                Button(
                                    onClick = { navController.navigate(Screen.BidSubmit.createRoute(job.id)) },
                                    modifier = Modifier.fillMaxWidth().height(50.dp)
                                ) {
                                    Text("Place a Bid")
                                }
                            }
                            job.status == "open" && hasAlreadyBid -> {
                                OutlinedButton(
                                    onClick = { navController.navigate(Screen.BidSubmit.createRoute(job.id)) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Edit Your Bid")
                                }
                            }
                            hasAlreadyBid && job.status in listOf("assigned", "in_progress") -> {
                                FilledTonalButton(
                                    onClick = { navController.navigate(Screen.ArtisanBookings.route) },
                                    modifier = Modifier.fillMaxWidth().height(50.dp)
                                ) {
                                    Text("View Your Booking")
                                }
                            }
                            job.status == "completed" -> {
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text("Job Completed") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    // AI Suggested Artisans — only shown to job owner on open jobs
                    if (isOwnJob && job.status == "open") {
                        Spacer(modifier = Modifier.height(24.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "✨ Suggested Artisans",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Powered by AI",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        if (isMatchLoading) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Finding the best artisans for you...", style = MaterialTheme.typography.bodySmall)
                            }
                        } else if (suggestedArtisans.isEmpty()) {
                            Text(
                                text = "No artisans in this category yet.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            suggestedArtisans.forEach { artisan ->
                                ElevatedCard(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(artisan.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                                Text(artisan.trade, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Text(artisan.serviceArea, style = MaterialTheme.typography.bodySmall)
                                            }
                                            Column(horizontalAlignment = Alignment.End) {
                                                if (artisan.reviewCount > 0) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(Icons.Filled.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                                        Text(String.format("%.1f", artisan.rating), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                                        Text(" (${artisan.reviewCount})", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                    }
                                                }
                                                Text(artisan.badge, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                            }
                                        }
                                        if (artisan.explanation.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = artisan.explanation,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Action buttons — customer (job owner) view
                    if (isOwnJob) {
                        when (job.status) {
                            "open" -> Button(
                                onClick = { navController.navigate(Screen.BidsList.createRoute(job.id)) },
                                modifier = Modifier.fillMaxWidth().height(50.dp)
                            ) {
                                Text(if (bidCount > 0) "View Bids ($bidCount)" else "View Bids (No bids yet)")
                            }
                            "assigned", "in_progress" -> FilledTonalButton(
                                onClick = { navController.navigate(Screen.CustomerBookings.route) },
                                modifier = Modifier.fillMaxWidth().height(50.dp)
                            ) {
                                Text("View Booking")
                            }
                            "completed" -> SuggestionChip(
                                onClick = {},
                                label = { Text("Job Completed") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }

    fullscreenImageUrl?.let { url ->
        FullscreenImageDialog(imageUrl = url, onDismiss = { fullscreenImageUrl = null })
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete this job?") },
            text = { Text("This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { showDeleteDialog = false; viewModel.deleteJob() }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
