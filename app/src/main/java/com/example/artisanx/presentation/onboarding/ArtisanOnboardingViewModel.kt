package com.example.artisanx.presentation.onboarding

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
class ArtisanOnboardingViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _phone = mutableStateOf("")
    val phone: State<String> = _phone

    private val _tradeCategory = mutableStateOf("")
    val tradeCategory: State<String> = _tradeCategory

    private val _skills = mutableStateOf("")
    val skills: State<String> = _skills

    private val _serviceArea = mutableStateOf("")
    val serviceArea: State<String> = _serviceArea

    private val _isStudent = mutableStateOf(false)
    val isStudent: State<Boolean> = _isStudent

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun onPhoneChange(value: String) { _phone.value = value }
    fun onTradeCategoryChange(value: String) { _tradeCategory.value = value }
    fun onSkillsChange(value: String) { _skills.value = value }
    fun onServiceAreaChange(value: String) { _serviceArea.value = value }
    fun onIsStudentChange(value: Boolean) { _isStudent.value = value }

    fun completeOnboarding() {
        if (_phone.value.isBlank() || _tradeCategory.value.isBlank() || _skills.value.isBlank() || _serviceArea.value.isBlank()) {
            viewModelScope.launch { _uiEvent.emit(UiEvent.ShowSnackbar("Please fill all fields")) }
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            val userRes = authRepository.getCurrentUser()
            if (userRes is Resource.Success && userRes.data != null) {
                val user = userRes.data
                val profileRes = profileRepository.createArtisanProfile(
                    userId = user.id,
                    fullName = user.name,
                    email = user.email,
                    phone = _phone.value,
                    tradeCategory = _tradeCategory.value,
                    skills = _skills.value,
                    serviceArea = _serviceArea.value,
                    isStudent = _isStudent.value
                )

                _isLoading.value = false
                if (profileRes is Resource.Success) {
                    _uiEvent.emit(UiEvent.Navigate("artisan_dashboard"))
                } else {
                    _uiEvent.emit(UiEvent.ShowSnackbar(profileRes.message ?: "Failed to create artisan profile"))
                }
            } else {
                _isLoading.value = false
                _uiEvent.emit(UiEvent.ShowSnackbar("Session expired. Please login again."))
            }
        }
    }

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
        data class Navigate(val route: String) : UiEvent()
    }
}
