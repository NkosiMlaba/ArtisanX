package com.example.artisanx.presentation.auth

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.artisanx.data.local.DataStoreManager
import com.example.artisanx.domain.repository.AuthRepository
import com.example.artisanx.domain.repository.ProfileRepository
import com.example.artisanx.presentation.navigation.Screen
import com.example.artisanx.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val _email = mutableStateOf("")
    val email: State<String> = _email

    private val _password = mutableStateOf("")
    val password: State<String> = _password

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _loginEventFlow = MutableSharedFlow<UiEvent>()
    val loginEventFlow = _loginEventFlow.asSharedFlow()

    fun onEmailChanged(value: String) {
        _email.value = value
    }

    fun onPasswordChanged(value: String) {
        _password.value = value
    }

    fun login() {
        if (email.value.isBlank() || password.value.isBlank()) {
            viewModelScope.launch {
                _loginEventFlow.emit(UiEvent.ShowSnackbar("Please fill in all fields."))
            }
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            val result = authRepository.login(email.value, password.value)
            when (result) {
                is Resource.Success -> {
                    // Notify global subscriptions to refresh for the new user
                    com.example.artisanx.util.SessionEventBus.emitSessionChanged()
                    // Determine the correct destination based on role
                    val destination = resolveDestination()
                    _isLoading.value = false
                    _loginEventFlow.emit(UiEvent.Navigate(destination))
                }
                is Resource.Error -> {
                    _isLoading.value = false
                    _loginEventFlow.emit(UiEvent.ShowSnackbar(result.message ?: "Login failed"))
                }
                else -> {
                    _isLoading.value = false
                }
            }
        }
    }

    private suspend fun resolveDestination(): String {
        // Get the current user to find their profile
        val userRes = authRepository.getCurrentUser()
        if (userRes !is Resource.Success || userRes.data == null) {
            return Screen.RoleSelection.route
        }
        val userId = userRes.data.id

        // Check if they have an artisan profile
        val artisanRes = profileRepository.getArtisanProfile(userId)
        if (artisanRes is Resource.Success) {
            dataStoreManager.saveUserRole("artisan")
            return Screen.ArtisanDashboard.route
        }

        // Check if they have a user profile with a role set
        val userProfileRes = profileRepository.getUserProfile(userId)
        if (userProfileRes is Resource.Success && userProfileRes.data != null) {
            val role = userProfileRes.data.data["role"] as? String
            if (role == "customer") {
                dataStoreManager.saveUserRole("customer")
                return Screen.CustomerDashboard.route
            }
            if (role == "artisan") {
                // Has user_profile with artisan role but no artisan_profile yet
                dataStoreManager.saveUserRole("artisan")
                return Screen.ArtisanOnboarding.route
            }
        }

        // No profile or role not set — go to role selection
        return Screen.RoleSelection.route
    }

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
        data class Navigate(val route: String) : UiEvent()
    }
}
