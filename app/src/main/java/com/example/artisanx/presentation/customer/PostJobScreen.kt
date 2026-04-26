package com.example.artisanx.presentation.customer

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.artisanx.presentation.common.LocationPickerScreen
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
    val photoUris = viewModel.photoUris
    val isPhotoUploading = viewModel.isPhotoUploading.value

    var showMapPicker by remember { mutableStateOf(false) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { viewModel.onPhotoSelected(it) } }

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is PostJobViewModel.UiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                is PostJobViewModel.UiEvent.NavigateBack -> navController.popBackStack()
            }
        }
    }

    if (showMapPicker) {
        LocationPickerScreen(
            onLocationSelected = { result ->
                viewModel.onLocationSelected(result.address, result.latitude, result.longitude)
                showMapPicker = false
            },
            onDismiss = { showMapPicker = false }
        )
        return
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
                supportingText = { Text("${description.length}/2000 — describe what needs doing") }
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

            // Location picker — shows map button + selected address
            OutlinedCard(
                onClick = { showMapPicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = if (address.isNotBlank()) MaterialTheme.colorScheme.primary
                               else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (address.isNotBlank()) address else "Tap to set job location on map",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (address.isNotBlank()) MaterialTheme.colorScheme.onSurface
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (address.isBlank()) {
                            Text(
                                text = "Location / Address",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = budget,
                onValueChange = viewModel::onBudgetChange,
                label = { Text("Budget (ZAR)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Photo section
            Text("Job Photos (optional, up to 3)", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                photoUris.forEachIndexed { index, uri ->
                    Box(modifier = Modifier.size(88.dp)) {
                        AsyncImage(
                            model = uri,
                            contentDescription = "Job photo",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(MaterialTheme.shapes.small),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { viewModel.removePhoto(index) },
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.TopEnd)
                                .background(MaterialTheme.colorScheme.errorContainer, CircleShape)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Remove photo",
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
                if (photoUris.size < 3) {
                    if (isPhotoUploading) {
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.small),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        }
                    } else {
                        OutlinedCard(
                            onClick = { photoPicker.launch("image/*") },
                            modifier = Modifier.size(88.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add photo", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.submitJob() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !isLoading && !isAiLoading && !isPhotoUploading
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
                Button(onClick = { viewModel.acceptAiSuggestion() }) { Text("Use This") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissAiSuggestion() }) { Text("Keep Mine") }
            }
        )
    }
}
