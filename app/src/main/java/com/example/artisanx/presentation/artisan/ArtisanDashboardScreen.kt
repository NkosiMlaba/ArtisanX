package com.example.artisanx.presentation.artisan

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.artisanx.domain.model.Bid
import androidx.compose.foundation.layout.PaddingValues
import com.example.artisanx.presentation.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtisanDashboardScreen(
    navController: NavController,
    viewModel: ArtisanDashboardViewModel = hiltViewModel()
) {
    val creditBalance = viewModel.creditBalance.value
    val myBids = viewModel.myBids.value
    val isLoading = viewModel.isLoading.value

    LaunchedEffect(key1 = true) {
        viewModel.loadDashboard()
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Artisan Dashboard") }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Credit balance card
            item {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(text = "Credit Balance", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$creditBalance credits",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Bidding on a job costs 2 credits",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = { navController.navigate(Screen.BuyCredits.route) },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Buy more credits →", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }

            // Quick actions
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FilledTonalButton(
                        onClick = { navController.navigate(Screen.JobBrowse.route) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Browse Jobs")
                    }
                    FilledTonalButton(
                        onClick = { navController.navigate(Screen.ArtisanBookings.route) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("My Bookings")
                    }
                }
            }

            // My bids section
            item {
                Text(
                    text = "My Recent Bids",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            } else if (myBids.isEmpty()) {
                item {
                    Text(
                        text = "No bids yet. Browse jobs to start bidding!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(myBids.take(10)) { bid ->
                    MyBidItem(bid = bid, onClick = {
                        navController.navigate(Screen.JobDetail.createRoute(bid.jobId))
                    })
                }
            }
        }
    }
}

@Composable
fun MyBidItem(bid: Bid, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "R${bid.priceOffer}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = "Est. ${bid.estimatedHours}h", style = MaterialTheme.typography.bodySmall)
            }
            val chipColor = when (bid.status) {
                "accepted" -> MaterialTheme.colorScheme.primary
                "rejected" -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.outline
            }
            SuggestionChip(
                onClick = {},
                label = { Text(bid.status.replaceFirstChar { it.uppercase() }) },
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = chipColor.copy(alpha = 0.12f),
                    labelColor = chipColor
                )
            )
        }
    }
}
