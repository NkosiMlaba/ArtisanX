package com.example.artisanx.presentation.profile

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val user = viewModel.user.value
    val isLoading = viewModel.isLoading.value

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collectLatest { event ->
            when(event) {
                is ProfileViewModel.UiEvent.Navigate -> {
                    navController.navigate(event.route) {
                        popUpTo(0) { inclusive = true } // Clear back stack on logout
                    }
                }
                else -> Unit
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Customer Profile") }) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            if (isLoading && user == null) {
                CircularProgressIndicator()
            } else if (user != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Name: ${user.name}", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Email: ${user.email}", style = MaterialTheme.typography.bodyLarge)
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Button(onClick = { viewModel.logout() }, enabled = !isLoading) {
                        if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        else Text("Logout")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtisanProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val user = viewModel.user.value
    val isLoading = viewModel.isLoading.value

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collectLatest { event ->
            when(event) {
                is ProfileViewModel.UiEvent.Navigate -> {
                    navController.navigate(event.route) {
                        popUpTo(0) { inclusive = true } 
                    }
                }
                else -> Unit
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Artisan Profile") }) }
    ) { padding ->
         Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            if (isLoading && user == null) {
                CircularProgressIndicator()
            } else if (user != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Name: ${user.name} (Artisan)", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Email: ${user.email}", style = MaterialTheme.typography.bodyLarge)
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Button(onClick = { viewModel.logout() }, enabled = !isLoading) {
                        if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        else Text("Logout")
                    }
                }
            }
        }
    }
}
