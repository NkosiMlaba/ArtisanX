package com.example.artisanx.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.artisanx.data.local.DataStoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoleSelectionViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager
) : ViewModel() {
    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun selectRole(role: String) {
        viewModelScope.launch {
            dataStoreManager.saveUserRole(role)
            if (role == "customer") {
                _uiEvent.emit(UiEvent.Navigate("customer_dashboard"))
            } else {
                _uiEvent.emit(UiEvent.Navigate("artisan_dashboard"))
            }
        }
    }

    sealed class UiEvent {
        data class Navigate(val route: String) : UiEvent()
    }
}
