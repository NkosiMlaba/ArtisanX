package com.example.artisanx.presentation.onboarding

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.artisanx.presentation.common.AuroraBackground
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtisanOnboardingScreen(
    navController: NavController,
    viewModel: ArtisanOnboardingViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState
) {
    val currentStep = viewModel.currentStep.value
    val isLoading = viewModel.isLoading.value

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is ArtisanOnboardingViewModel.UiEvent.ShowSnackbar ->
                    snackbarHostState.showSnackbar(event.message)
                is ArtisanOnboardingViewModel.UiEvent.Navigate ->
                    navController.navigate(event.route) {
                        popUpTo("artisan_onboarding") { inclusive = true }
                    }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
    AuroraBackground()
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Artisan Profile Setup") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (currentStep != OnboardingStep.BASIC) {
                        OutlinedButton(
                            onClick = { viewModel.previousStep() },
                            modifier = Modifier.weight(1f),
                            enabled = !isLoading
                        ) { Text("Back") }
                    }
                    Button(
                        onClick = { viewModel.nextStep() },
                        modifier = Modifier.weight(if (currentStep == OnboardingStep.BASIC) 1f else 1f),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(if (currentStep == OnboardingStep.PORTFOLIO) "Complete Profile" else "Next")
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OnboardingStepper(currentStep = currentStep)

            when (currentStep) {
                OnboardingStep.BASIC -> StepBasic(viewModel)
                OnboardingStep.IDENTITY -> if (viewModel.isStudent.value) {
                    StepStudent(viewModel)
                } else {
                    StepIndependent(viewModel)
                }
                OnboardingStep.PORTFOLIO -> StepPortfolio(viewModel)
            }
        }
    }
    }
}

@Composable
private fun OnboardingStepper(currentStep: OnboardingStep) {
    val steps = listOf("Basic Info", "Identity", "Portfolio")
    val current = currentStep.ordinal

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, label ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                index < current -> MaterialTheme.colorScheme.tertiary
                                index == current -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (index < current) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiary,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(
                            text = "${index + 1}",
                            color = if (index == current)
                                MaterialTheme.colorScheme.onPrimary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (index == current) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (index < steps.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = if (index < current) MaterialTheme.colorScheme.tertiary
                    else MaterialTheme.colorScheme.outlineVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StepBasic(viewModel: ArtisanOnboardingViewModel) {
    var categoryExpanded by remember { mutableStateOf(false) }
    val selectedCategory = viewModel.tradeCategory.value

    Text("Tell us about your trade", style = MaterialTheme.typography.titleMedium)

    OutlinedTextField(
        value = viewModel.phone.value,
        onValueChange = viewModel::onPhoneChange,
        label = { Text("Phone Number *") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )

    ExposedDropdownMenuBox(
        expanded = categoryExpanded,
        onExpandedChange = { categoryExpanded = it }
    ) {
        OutlinedTextField(
            value = selectedCategory,
            onValueChange = {},
            readOnly = true,
            label = { Text("Trade Category *") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = categoryExpanded,
            onDismissRequest = { categoryExpanded = false }
        ) {
            ArtisanOnboardingViewModel.TRADE_CATEGORIES.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category) },
                    onClick = {
                        viewModel.onTradeCategoryChange(category)
                        categoryExpanded = false
                    }
                )
            }
        }
    }

    OutlinedTextField(
        value = viewModel.serviceArea.value,
        onValueChange = viewModel::onServiceAreaChange,
        label = { Text("Service Area * (e.g. Johannesburg Central)") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (viewModel.isStudent.value)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Student Artisan", style = MaterialTheme.typography.titleSmall)
                Text(
                    "Currently studying a trade at a college or institution",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = viewModel.isStudent.value,
                onCheckedChange = viewModel::onIsStudentChange
            )
        }
    }
}

@Composable
private fun StepStudent(viewModel: ArtisanOnboardingViewModel) {
    val studentCardLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { viewModel.onStudentCardSelected(it) } }

    Text("Student Details", style = MaterialTheme.typography.titleMedium)
    Text(
        "As a student artisan you'll be able to connect with customers at reduced rates.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    OutlinedTextField(
        value = viewModel.institutionName.value,
        onValueChange = viewModel::onInstitutionNameChange,
        label = { Text("Institution Name *") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )

    OutlinedTextField(
        value = viewModel.studentNumber.value,
        onValueChange = viewModel::onStudentNumberChange,
        label = { Text("Student Number *") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )

    OutlinedTextField(
        value = viewModel.courseField.value,
        onValueChange = viewModel::onCourseFieldChange,
        label = { Text("Course / Field of Study") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )

    OutlinedTextField(
        value = viewModel.gradYear.value,
        onValueChange = viewModel::onGradYearChange,
        label = { Text("Expected Graduation Year") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )

    FileUploadCard(
        label = "Student Card (optional)",
        uri = viewModel.studentCardUri.value,
        uploading = viewModel.studentCardUploading.value,
        onPickFile = { studentCardLauncher.launch("image/*") }
    )
}

@Composable
private fun StepIndependent(viewModel: ArtisanOnboardingViewModel) {
    val idDocLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { viewModel.onIdDocSelected(it) } }

    Text("Independent Artisan Details", style = MaterialTheme.typography.titleMedium)

    OutlinedTextField(
        value = viewModel.yearsExperience.value,
        onValueChange = viewModel::onYearsExperienceChange,
        label = { Text("Years of Experience *") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )

    OutlinedTextField(
        value = viewModel.certifications.value,
        onValueChange = viewModel::onCertificationsChange,
        label = { Text("Certifications / Qualifications") },
        modifier = Modifier.fillMaxWidth().height(100.dp),
        maxLines = 3
    )

    FileUploadCard(
        label = "ID Document (optional)",
        uri = viewModel.idDocUri.value,
        uploading = viewModel.idDocUploading.value,
        onPickFile = { idDocLauncher.launch("image/*") }
    )
}

@Composable
private fun StepPortfolio(viewModel: ArtisanOnboardingViewModel) {
    val workPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { viewModel.onWorkPhotoSelected(it) } }

    val skillsText = viewModel.skills.value

    Text("Your Skills & Portfolio", style = MaterialTheme.typography.titleMedium)

    OutlinedTextField(
        value = skillsText,
        onValueChange = viewModel::onSkillsChange,
        label = { Text("Describe your skills and experience *") },
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        maxLines = 6,
        supportingText = {
            Text(
                "${skillsText.length}/2000",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
        }
    )

    Text("Work Photos (up to 3)", style = MaterialTheme.typography.labelLarge)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        viewModel.workPhotoUris.forEachIndexed { index, uri ->
            Box(modifier = Modifier.size(88.dp)) {
                AsyncImage(
                    model = uri,
                    contentDescription = "Work photo",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(MaterialTheme.shapes.small),
                    contentScale = ContentScale.Crop
                )
                IconButton(
                    onClick = { viewModel.removeWorkPhoto(index) },
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.TopEnd)
                        .background(MaterialTheme.colorScheme.errorContainer, CircleShape)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Remove",
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        if (viewModel.workPhotoUris.size < 3) {
            if (viewModel.workPhotoUploading.value) {
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
                    onClick = { workPhotoLauncher.launch("image/*") },
                    modifier = Modifier.size(88.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add work photo",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FileUploadCard(
    label: String,
    uri: Uri?,
    uploading: Boolean,
    onPickFile: () -> Unit
) {
    OutlinedCard(
        onClick = onPickFile,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when {
                uploading -> CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                uri != null -> Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary
                )
                else -> Icon(
                    Icons.Default.UploadFile,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Column {
                Text(label, style = MaterialTheme.typography.bodyMedium)
                Text(
                    when {
                        uploading -> "Uploading..."
                        uri != null -> "Uploaded — tap to replace"
                        else -> "Tap to upload"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
