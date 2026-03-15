package com.example.artisanx.presentation.auth

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.artisanx.domain.repository.AuthRepository
import com.example.artisanx.domain.repository.ProfileRepository
import com.example.artisanx.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _name = mutableStateOf("")
    val name: State<String> = _name

    private val _email = mutableStateOf("")
    val email: State<String> = _email

    private val _password = mutableStateOf("")
    val password: State<String> = _password

    private val _confirmPassword = mutableStateOf("")
    val confirmPassword: State<String> = _confirmPassword

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _registerEventFlow = MutableSharedFlow<UiEvent>()
    val registerEventFlow = _registerEventFlow.asSharedFlow()

    fun onNameChanged(value: String) { _name.value = value }
    fun onEmailChanged(value: String) { _email.value = value }
    fun onPasswordChanged(value: String) { _password.value = value }
    fun onConfirmPasswordChanged(value: String) { _confirmPassword.value = value }

    fun register() {
        if (name.value.isBlank() || email.value.isBlank() || password.value.isBlank()) {
            emitError("Please fill in all fields.")
            return
        }
        if (password.value != confirmPassword.value) {
            emitError("Passwords do not match.")
            return
        }
        if (password.value.length < 8) {
            emitError("Password must be at least 8 characters.")
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            val registerResult = authRepository.register(email.value, password.value, name.value)
            
            when (registerResult) {
                is Resource.Success -> {
                    // Auto-login after successful registration
                    val loginResult = authRepository.login(email.value, password.value)
                    when (loginResult) {
                        is Resource.Success -> {
                            // Create user profile document
                            val userId = registerResult.data?.id ?: ""
                            val profileRes = profileRepository.createUserProfile(
                                userId = userId,
                                fullName = name.value,
                                email = email.value,
                                role = "pending" // Role will be set during onboarding
                            )
                            _isLoading.value = false
                            if (profileRes is Resource.Success) {
                                _registerEventFlow.emit(UiEvent.Navigate("role_selection"))
                            } else {
                                _registerEventFlow.emit(UiEvent.ShowSnackbar(profileRes.message ?: "Registration succeeded, but profile creation failed."))
                            }
                        }
                        is Resource.Error -> {
                            _isLoading.value = false
                            _registerEventFlow.emit(UiEvent.ShowSnackbar(loginResult.message ?: "Registration succeeded, but auto-login failed."))
                        }
                        else -> {
                            _isLoading.value = false
                        }
                    }
                }
                is Resource.Error -> {
                    _isLoading.value = false
                    _registerEventFlow.emit(UiEvent.ShowSnackbar(registerResult.message ?: "Registration failed"))
                }
                else -> {
                    _isLoading.value = false
                }
            }
        }
    }

    private fun emitError(message: String) {
        viewModelScope.launch {
            _registerEventFlow.emit(UiEvent.ShowSnackbar(message))
        }
    }

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
        data class Navigate(val route: String) : UiEvent()
    }
}
