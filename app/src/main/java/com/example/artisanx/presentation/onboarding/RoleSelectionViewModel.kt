package com.example.artisanx.presentation.onboarding

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
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoleSelectionViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager,
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {
    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun selectRole(role: String) {
        viewModelScope.launch {
            dataStoreManager.saveUserRole(role)
            
            // Sync with Appwrite
            val userRes = authRepository.getCurrentUser()
            if (userRes is Resource.Success) {
                val userId = userRes.data?.id ?: ""
                profileRepository.updateUserProfile(userId, mapOf("role" to role))
            }

            if (role == "customer") {
                _uiEvent.emit(UiEvent.Navigate(Screen.CustomerDashboard.route))
            } else {
                _uiEvent.emit(UiEvent.Navigate(Screen.ArtisanOnboarding.route))
            }
        }
    }

    sealed class UiEvent {
        data class Navigate(val route: String) : UiEvent()
    }
}
