package com.example.artisanx.presentation.auth

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.artisanx.domain.repository.AuthRepository
import com.example.artisanx.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
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
            _isLoading.value = false
            when (result) {
                is Resource.Success -> {
                    _loginEventFlow.emit(UiEvent.Navigate("home"))
                }
                is Resource.Error -> {
                    _loginEventFlow.emit(UiEvent.ShowSnackbar(result.message ?: "Login failed"))
                }
                else -> Unit
            }
        }
    }

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
        data class Navigate(val route: String) : UiEvent()
    }
}
