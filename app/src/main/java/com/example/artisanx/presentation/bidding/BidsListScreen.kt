package com.example.artisanx.presentation.bidding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.artisanx.domain.model.Bid
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BidsListScreen(
    navController: NavController,
    viewModel: BidsListViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState
) {
    val job = viewModel.job.value
    val bids = viewModel.bids.value
    val isLoading = viewModel.isLoading.value

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is BidsListViewModel.UiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bids" + if (job != null) " — ${job.title}" else "") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (bids.isEmpty()) {
                Text(
                    text = "No bids yet. Your job is visible to artisans in the area.",
                    modifier = Modifier.align(Alignment.Center).padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                val jobIsAssigned = job?.status == "assigned"
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(bids) { bid ->
                        BidCard(bid = bid, jobAssigned = jobIsAssigned, onAccept = { viewModel.acceptBid(bid) })
                    }
                }
            }
        }
    }
}

@Composable
fun BidCard(bid: Bid, jobAssigned: Boolean, onAccept: () -> Unit) {
    var showConfirmDialog by remember { mutableStateOf(false) }

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "R${bid.priceOffer}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                StatusChip(status = bid.status)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Est. ${bid.estimatedHours} hours", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = bid.message, style = MaterialTheme.typography.bodyMedium)

            if (bid.status == "pending" && !jobAssigned) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { showConfirmDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Accept Bid")
                }
            }
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Accept this bid?") },
            text = { Text("Accepting this bid will reject all other bids and create a booking for R${bid.priceOffer}.") },
            confirmButton = {
                TextButton(onClick = { showConfirmDialog = false; onAccept() }) {
                    Text("Accept")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun StatusChip(status: String) {
    val color = when (status) {
        "accepted" -> MaterialTheme.colorScheme.primary
        "rejected" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.outline
    }
    SuggestionChip(
        onClick = {},
        label = { Text(status.replaceFirstChar { it.uppercase() }) },
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = color.copy(alpha = 0.12f),
            labelColor = color
        )
    )
}
