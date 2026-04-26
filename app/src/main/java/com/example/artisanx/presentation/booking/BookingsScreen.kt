package com.example.artisanx.presentation.booking

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.artisanx.presentation.navigation.Screen
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingsScreen(
    isArtisan: Boolean,
    navController: NavController,
    viewModel: BookingsViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState
) {
    val bookings = viewModel.bookings.value
    val isLoading = viewModel.isLoading.value
    val error = viewModel.error.value

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is BookingsViewModel.UiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("My Bookings") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = { viewModel.loadBookings() },
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            if (error != null) {
                Text(text = error, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
            } else if (bookings.isEmpty() && !isLoading) {
                Text(text = "No bookings yet.", modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(bookings) { bookingWithJob ->
                        BookingCard(
                            bookingWithJob = bookingWithJob,
                            isArtisan = isArtisan,
                            onUpdateStatus = { newStatus -> viewModel.updateStatus(bookingWithJob.booking.id, newStatus) },
                            onMarkPaid = { viewModel.markAsPaid(bookingWithJob.booking.id) },
                            onOpenChat = {
                                navController.navigate(Screen.Chat.createRoute(bookingWithJob.booking.id))
                            },
                            onLeaveReview = {
                                navController.navigate(
                                    Screen.Review.createRoute(
                                        bookingWithJob.booking.id,
                                        bookingWithJob.booking.artisanId
                                    )
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BookingCard(
    bookingWithJob: BookingWithJob,
    isArtisan: Boolean,
    onUpdateStatus: (String) -> Unit,
    onMarkPaid: () -> Unit,
    onOpenChat: () -> Unit,
    onLeaveReview: () -> Unit
) {
    val booking = bookingWithJob.booking
    val job = bookingWithJob.job

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = job?.title ?: "Job #${booking.jobId.take(8)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if (job != null) {
                Text(
                    text = job.category,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            BookingStatusStepper(currentStatus = booking.status)

            Spacer(modifier = Modifier.height(12.dp))

            if (booking.isPaid) {
                SuggestionChip(
                    onClick = {},
                    label = { Text("Paid") },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Chat button — always available for active bookings
            if (booking.status != "requested") {
                OutlinedButton(onClick = onOpenChat, modifier = Modifier.fillMaxWidth()) {
                    Text("Open Chat")
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Role-specific action buttons
            if (isArtisan) {
                when (booking.status) {
                    "requested" -> Button(
                        onClick = { onUpdateStatus("accepted") },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Accept Booking") }
                    "accepted" -> Button(
                        onClick = { onUpdateStatus("in_progress") },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Start Job") }
                    "in_progress" -> Button(
                        onClick = { onUpdateStatus("completed") },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Mark Complete") }
                }
            } else {
                // Customer actions
                if (booking.status == "completed") {
                    if (!booking.isPaid) {
                        OutlinedButton(onClick = onMarkPaid, modifier = Modifier.fillMaxWidth()) {
                            Text("Mark as Paid")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Button(onClick = onLeaveReview, modifier = Modifier.fillMaxWidth()) {
                        Text("Leave a Review")
                    }
                }
            }
        }
    }
}

@Composable
fun BookingStatusStepper(currentStatus: String) {
    val steps = listOf("requested", "accepted", "in_progress", "completed")
    val currentIndex = steps.indexOf(currentStatus).coerceAtLeast(0)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        steps.forEachIndexed { index, step ->
            val isActive = index <= currentIndex
            val color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
            val label = step.replace("_", " ").replaceFirstChar { it.uppercase() }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "${index + 1}",
                            color = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = label, style = MaterialTheme.typography.labelSmall, color = color)
            }
        }
    }
}
