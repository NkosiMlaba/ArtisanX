package com.example.artisanx.presentation.artisan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.artisanx.domain.model.Job
import com.example.artisanx.presentation.navigation.Screen
import com.example.artisanx.presentation.common.AuroraBackground
import com.example.artisanx.presentation.common.EmptyState
import com.example.artisanx.presentation.common.JobCardSkeleton
import com.example.artisanx.presentation.common.iconForCategory
import com.example.artisanx.util.LocationUtils
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobBrowseScreen(
    navController: NavController,
    viewModel: JobBrowseViewModel = hiltViewModel()
) {
    val jobs = viewModel.jobs.value
    val isLoading = viewModel.isLoading.value
    val error = viewModel.error.value
    val selectedCategory = viewModel.selectedCategory.value
    val selectedSort = viewModel.selectedSort.value
    val searchQuery = viewModel.searchQuery.value

    var sortMenuExpanded by remember { mutableStateOf(false) }
    var isMapView by remember { mutableStateOf(false) }
    var selectedMapJob by remember { mutableStateOf<Job?>(null) }

    val categories = listOf("All", "Plumbing", "Electrical", "Cleaning", "Carpentry", "Painting", "Tiling", "Roofing", "General", "Other")

    if (selectedMapJob != null) {
        val job = selectedMapJob!!
        AlertDialog(
            onDismissRequest = { selectedMapJob = null },
            title = { Text(job.title, style = MaterialTheme.typography.titleMedium) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(job.category, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    if (job.budget > 0) Text("Budget: R${job.budget.toInt()}", style = MaterialTheme.typography.bodyMedium)
                    if (job.address.isNotBlank()) Text(job.address, style = MaterialTheme.typography.bodySmall)
                    viewModel.distanceKmFor(job)?.let { km ->
                        Text(LocationUtils.formatDistanceKm(km), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    selectedMapJob = null
                    navController.navigate(Screen.JobDetail.createRoute(job.id))
                }) { Text("View Details") }
            },
            dismissButton = {
                TextButton(onClick = { selectedMapJob = null }) { Text("Close") }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AuroraBackground()
        Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Browse Open Jobs") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                ),
                actions = {
                    IconButton(onClick = { isMapView = !isMapView }) {
                        Icon(
                            imageVector = if (isMapView) Icons.Default.List else Icons.Default.Map,
                            contentDescription = if (isMapView) "List view" else "Map view"
                        )
                    }
                    Box {
                        TextButton(
                            onClick = { sortMenuExpanded = true }
                        ) {
                            Text(selectedSort.label, style = MaterialTheme.typography.labelMedium)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Sort")
                        }
                        DropdownMenu(
                            expanded = sortMenuExpanded,
                            onDismissRequest = { sortMenuExpanded = false }
                        ) {
                            JobSortOption.entries.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.label) },
                                    onClick = {
                                        viewModel.setSortOption(option)
                                        sortMenuExpanded = false
                                    },
                                    trailingIcon = {
                                        if (option == selectedSort) {
                                            RadioButton(selected = true, onClick = null)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search jobs by title, category, location…") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.extraLarge
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    val isSelected = if (category == "All") selectedCategory == null else selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            viewModel.setCategoryFilter(if (category == "All") null else category)
                        },
                        label = { Text(category) }
                    )
                }
            }

            if (isMapView) {
                val jobsWithCoords = jobs.filter { it.latitude != 0.0 || it.longitude != 0.0 }
                val center = jobsWithCoords.firstOrNull()
                    ?.let { LatLng(it.latitude, it.longitude) }
                    ?: LatLng(-29.8587, 31.0218)
                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(center, 11f)
                }
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState
                ) {
                    jobsWithCoords.forEach { job ->
                        Marker(
                            state = MarkerState(position = LatLng(job.latitude, job.longitude)),
                            title = job.title,
                            snippet = "R${job.budget.toInt()} • ${job.category}",
                            onClick = { selectedMapJob = job; false }
                        )
                    }
                }
            } else {
                PullToRefreshBox(
                    isRefreshing = isLoading,
                    onRefresh = { viewModel.loadJobs() },
                    modifier = Modifier.fillMaxSize()
                ) {
                    when {
                        isLoading && jobs.isEmpty() -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(5) { JobCardSkeleton() }
                            }
                        }
                        error != null -> {
                            Text(text = error, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center).padding(16.dp))
                        }
                        jobs.isEmpty() -> {
                            EmptyState(
                                icon = Icons.Default.Handyman,
                                title = "No open jobs in this category",
                                subtitle = "Check back later or try a different category.",
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                        else -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(jobs) { job ->
                                    JobItemBrowse(
                                        job = job,
                                        distanceKm = viewModel.distanceKmFor(job),
                                        onClick = { navController.navigate(Screen.JobDetail.createRoute(job.id)) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        }
    }
}

@Composable
fun JobItemBrowse(job: Job, distanceKm: Double? = null, onClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconForCategory(job.category),
                        contentDescription = job.category,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = job.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = job.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (job.budget > 0) {
                    Box(
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.small)
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "R${job.budget.toInt()}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (job.address.isNotBlank()) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = job.address,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
                if (distanceKm != null) {
                    Text(
                        text = LocationUtils.formatDistanceKm(distanceKm),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
                if (job.urgency == "urgent") {
                    Spacer(modifier = Modifier.width(8.dp))
                    Row(
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.small)
                            .background(MaterialTheme.colorScheme.errorContainer)
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Bolt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Urgent",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
}
