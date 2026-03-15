package com.example.artisanx.presentation.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.flow.collectLatest

@Composable
fun RoleSelectionScreen(
    navController: NavController,
    viewModel: RoleSelectionViewModel = hiltViewModel()
) {
    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collectLatest { event ->
            when(event) {
                is RoleSelectionViewModel.UiEvent.Navigate -> {
                    navController.navigate(event.route) {
                        popUpTo("role_selection") { inclusive = true }
                    }
                }
            }
        }
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Choose Your Role",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { viewModel.selectRole("customer") },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("I want to hire Artisans")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.selectRole("artisan") },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("I am an Artisan / Worker")
            }
        }
    }
}
