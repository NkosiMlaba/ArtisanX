package com.example.artisanx.presentation.profile

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.artisanx.data.local.DataStoreManager
import com.example.artisanx.domain.repository.AuthRepository
import com.example.artisanx.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import io.appwrite.models.User
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val _user = mutableStateOf<User<Map<String, Any>>?>(null)
    val user: State<User<Map<String, Any>>?> = _user

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        _isLoading.value = true
        viewModelScope.launch {
            when (val res = authRepository.getCurrentUser()) {
                is Resource.Success -> _user.value = res.data
                is Resource.Error -> _uiEvent.emit(UiEvent.ShowSnackbar(res.message ?: "Failed to load profile"))
                else -> Unit
            }
            _isLoading.value = false
        }
    }

    fun logout() {
        _isLoading.value = true
        viewModelScope.launch {
            when (val res = authRepository.logout()) {
                is Resource.Success -> {
                    dataStoreManager.clearRole()
                    _uiEvent.emit(UiEvent.Navigate("login"))
                }
                is Resource.Error -> {
                    _uiEvent.emit(UiEvent.ShowSnackbar(res.message ?: "Failed to logout"))
                }
                else -> Unit
            }
            _isLoading.value = false
        }
    }

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
        data class Navigate(val route: String) : UiEvent()
    }
}
