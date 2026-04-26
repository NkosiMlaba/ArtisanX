package com.example.artisanx.presentation.artisan

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.artisanx.domain.model.Job
import com.example.artisanx.presentation.navigation.Screen
import com.example.artisanx.util.LocationUtils

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

    var sortMenuExpanded by remember { mutableStateOf(false) }

    val categories = listOf("All", "Plumbing", "Electrical", "Cleaning", "Carpentry", "Painting", "Tiling", "Roofing", "General", "Other")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Browse Open Jobs") },
                actions = {
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
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
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

            PullToRefreshBox(
                isRefreshing = isLoading,
                onRefresh = { viewModel.loadJobs() },
                modifier = Modifier.fillMaxSize()
            ) {
                if (error != null) {
                    Text(text = error, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
                } else if (jobs.isEmpty() && !isLoading) {
                    Text(text = "No open jobs found in this category.", modifier = Modifier.align(Alignment.Center))
                } else {
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

@Composable
fun JobItemBrowse(job: Job, distanceKm: Double? = null, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = job.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (job.budget > 0) {
                    SuggestionChip(
                        onClick = {},
                        label = { Text("R${job.budget.toInt()}", style = MaterialTheme.typography.labelMedium) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = job.category, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                if (job.urgency == "urgent") {
                    Text(
                        text = "⚡ Urgent",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            if (job.address.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = job.address,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (distanceKm != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = LocationUtils.formatDistanceKm(distanceKm),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
