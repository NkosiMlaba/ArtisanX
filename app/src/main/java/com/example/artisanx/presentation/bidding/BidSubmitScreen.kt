package com.example.artisanx.presentation.bidding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BidSubmitScreen(
    navController: NavController,
    viewModel: BidSubmitViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState
) {
    val job = viewModel.job.value
    val priceOffer = viewModel.priceOffer.value
    val message = viewModel.message.value
    val estimatedHours = viewModel.estimatedHours.value
    val isLoading = viewModel.isLoading.value
    val credits = viewModel.creditBalance.value

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is BidSubmitViewModel.UiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                is BidSubmitViewModel.UiEvent.NavigateBack -> navController.popBackStack()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Submit a Bid") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Job summary card
            if (job != null) {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = job.title, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = job.category, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (job.budget > 0) {
                            Text(text = "Budget: R${job.budget}", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Credit balance indicator
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Credits: $credits",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "Cost: 2 credits",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = priceOffer,
                onValueChange = viewModel::onPriceOfferChange,
                label = { Text("Your Price (ZAR)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = estimatedHours,
                onValueChange = viewModel::onEstimatedHoursChange,
                label = { Text("Estimated Hours") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = message,
                onValueChange = viewModel::onMessageChange,
                label = { Text("Message to Customer") },
                modifier = Modifier.fillMaxWidth().height(150.dp),
                maxLines = 5,
                supportingText = { Text("${message.length}/500") }
            )
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.submitBid() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !isLoading && credits >= 2
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                } else {
                    Text("Submit Bid")
                }
            }

            if (credits < 2) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "You need at least 2 credits to bid.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
