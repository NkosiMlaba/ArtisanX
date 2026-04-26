package com.example.artisanx.presentation.customer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.artisanx.domain.model.Job
import com.example.artisanx.presentation.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDashboardScreen(
    navController: NavController,
    viewModel: CustomerDashboardViewModel = hiltViewModel()
) {
    val jobs = viewModel.jobs.value
    val isLoading = viewModel.isLoading.value
    val error = viewModel.error.value

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("My Posted Jobs") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Screen.PostJob.route) }) {
                Icon(Icons.Default.Add, contentDescription = "Post new job")
            }
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = { viewModel.loadMyJobs() },
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            if (error != null && !isLoading) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
            } else if (jobs.isEmpty() && !isLoading) {
                Text(
                    text = "You haven't posted any jobs yet.\nTap + to post your first job.",
                    modifier = Modifier.align(Alignment.Center).padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(jobs) { job ->
                        JobItem(job = job, onClick = {
                            navController.navigate(Screen.JobDetail.createRoute(job.id))
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun JobItem(job: Job, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = job.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                val statusColor = when (job.status) {
                    "open" -> MaterialTheme.colorScheme.primary
                    "assigned", "in_progress" -> MaterialTheme.colorScheme.tertiary
                    "completed" -> MaterialTheme.colorScheme.outline
                    else -> MaterialTheme.colorScheme.error
                }
                SuggestionChip(
                    onClick = {},
                    label = { Text(job.status.replace("_", " ").replaceFirstChar { it.uppercase() }) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = statusColor.copy(alpha = 0.12f),
                        labelColor = statusColor
                    )
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = job.category, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (job.budget > 0) {
                Text(text = "Budget: R${job.budget}", style = MaterialTheme.typography.bodyMedium)
            }
            if (job.address.isNotBlank()) {
                Text(text = job.address, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
