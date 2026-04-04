package com.example.artisanx.presentation.common

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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

    var showDeleteDialog by remember { mutableStateOf(false) }

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

                    Text(text = "Job Type: ${job.jobType.replaceFirstChar { it.uppercase() }}", style = MaterialTheme.typography.bodyMedium)

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(text = "Description", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = job.description, style = MaterialTheme.typography.bodyLarge)

                    Spacer(modifier = Modifier.height(24.dp))

                    // Action buttons based on role
                    if (userRole == "artisan" && job.status == "open") {
                        if (hasAlreadyBid) {
                            OutlinedButton(
                                onClick = {},
                                modifier = Modifier.fillMaxWidth(),
                                enabled = false
                            ) {
                                Text("Already Bid on This Job")
                            }
                        } else {
                            Button(
                                onClick = { navController.navigate(Screen.BidSubmit.createRoute(job.id)) },
                                modifier = Modifier.fillMaxWidth().height(50.dp)
                            ) {
                                Text("Place a Bid")
                            }
                        }
                    }

                    if (isOwnJob && job.status == "open") {
                        Button(
                            onClick = { navController.navigate(Screen.BidsList.createRoute(job.id)) },
                            modifier = Modifier.fillMaxWidth().height(50.dp)
                        ) {
                            Text("View Bids ($bidCount)")
                        }
                    }
                }
            }
        }
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
