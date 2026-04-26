package com.example.artisanx.presentation.customer

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
fun PostJobScreen(
    navController: NavController,
    viewModel: PostJobViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState
) {
    val title = viewModel.title.value
    val description = viewModel.description.value
    val category = viewModel.category.value
    val address = viewModel.address.value
    val budget = viewModel.budget.value
    val isLoading = viewModel.isLoading.value
    val isAiLoading = viewModel.isAiLoading.value
    val aiSuggestion = viewModel.aiSuggestion.value

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is PostJobViewModel.UiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                is PostJobViewModel.UiEvent.NavigateBack -> navController.popBackStack()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Post a Job") },
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
            OutlinedTextField(
                value = title,
                onValueChange = viewModel::onTitleChange,
                label = { Text("Job Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = category,
                onValueChange = viewModel::onCategoryChange,
                label = { Text("Category (e.g. Plumbing, Electrical)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = description,
                onValueChange = viewModel::onDescriptionChange,
                label = { Text("Job Description") },
                modifier = Modifier.fillMaxWidth().height(140.dp),
                maxLines = 5,
                supportingText = { Text("Describe what you need done") }
            )

            // AI description button
            Spacer(modifier = Modifier.height(4.dp))
            FilledTonalButton(
                onClick = { viewModel.generateAiDescription() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isAiLoading && !isLoading
            ) {
                if (isAiLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Improving description...")
                } else {
                    Text("✨ Help me describe this job")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = address,
                onValueChange = viewModel::onAddressChange,
                label = { Text("Location / Address") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = budget,
                onValueChange = viewModel::onBudgetChange,
                label = { Text("Budget (ZAR)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.submitJob() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !isLoading && !isAiLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                } else {
                    Text("Post Job")
                }
            }
        }
    }

    // AI suggestion dialog
    if (aiSuggestion != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissAiSuggestion() },
            title = { Text("✨ AI-Generated Description") },
            text = {
                Column {
                    Text(
                        text = "Powered by AI — review and edit before using:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = aiSuggestion, style = MaterialTheme.typography.bodyMedium)
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.acceptAiSuggestion() }) {
                    Text("Use This")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissAiSuggestion() }) {
                    Text("Keep Mine")
                }
            }
        )
    }
}
