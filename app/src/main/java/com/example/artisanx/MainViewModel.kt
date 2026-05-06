package com.example.artisanx

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.artisanx.data.local.DataStoreManager
import com.example.artisanx.domain.repository.AuthRepository
import com.example.artisanx.presentation.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

    private val _startDestination = mutableStateOf(Screen.Login.route)
    val startDestination: State<String> = _startDestination

    private val _pendingDeepLinkBookingId = mutableStateOf<String?>(null)
    val pendingDeepLinkBookingId: State<String?> = _pendingDeepLinkBookingId

    private val _pendingDeepLinkJobId = mutableStateOf<String?>(null)
    val pendingDeepLinkJobId: State<String?> = _pendingDeepLinkJobId

    fun setDeepLink(bookingId: String?) {
        _pendingDeepLinkBookingId.value = bookingId
    }

    fun setJobDeepLink(jobId: String?) {
        _pendingDeepLinkJobId.value = jobId
    }

    fun clearDeepLink() {
        _pendingDeepLinkBookingId.value = null
        _pendingDeepLinkJobId.value = null
    }

    init {
        viewModelScope.launch {
            val isLoggedIn = authRepository.isLoggedIn()
            if (isLoggedIn) {
                val role = dataStoreManager.userRoleFlow.first()
                if (role == "customer") {
                    _startDestination.value = Screen.CustomerDashboard.route
                } else if (role == "artisan") {
                    _startDestination.value = Screen.ArtisanDashboard.route
                } else {
                    _startDestination.value = Screen.RoleSelection.route
                }
            } else {
                _startDestination.value = Screen.Login.route
            }
            _isLoading.value = false
        }
    }
}
