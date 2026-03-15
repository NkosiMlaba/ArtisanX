package com.example.artisanx.presentation.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
fun ArtisanOnboardingScreen(
    navController: NavController,
    viewModel: ArtisanOnboardingViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState
) {
    val phone = viewModel.phone.value
    val tradeCategory = viewModel.tradeCategory.value
    val skills = viewModel.skills.value
    val serviceArea = viewModel.serviceArea.value
    val isStudent = viewModel.isStudent.value
    val isLoading = viewModel.isLoading.value

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collectLatest { event ->
            when(event) {
                is ArtisanOnboardingViewModel.UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is ArtisanOnboardingViewModel.UiEvent.Navigate -> {
                    navController.navigate(event.route) {
                        popUpTo("artisan_onboarding") { inclusive = true }
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Artisan Onboarding") }) },
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
            Text(
                text = "Tell us about your trade",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = viewModel::onPhoneChange,
                label = { Text("Phone Number") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = tradeCategory,
                onValueChange = viewModel::onTradeCategoryChange,
                label = { Text("Trade Category (e.g. Plumber)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = skills,
                onValueChange = viewModel::onSkillsChange,
                label = { Text("Description of Skills") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                maxLines = 4
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = serviceArea,
                onValueChange = viewModel::onServiceAreaChange,
                label = { Text("Service Area (e.g. Johannesburg Central)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isStudent,
                    onCheckedChange = viewModel::onIsStudentChange
                )
                Text(text = "I am a student artisan")
            }
            
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { viewModel.completeOnboarding() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                } else {
                    Text("Complete Profile")
                }
            }
        }
    }
}
