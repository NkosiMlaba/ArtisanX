package com.example.artisanx.presentation.credits

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.artisanx.domain.repository.AuthRepository
import com.example.artisanx.domain.repository.CreditsRepository
import com.example.artisanx.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BuyCreditsViewModel @Inject constructor(
    private val creditsRepository: CreditsRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _balance = mutableIntStateOf(0)
    val balance: State<Int> = _balance

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    init {
        loadBalance()
    }

    private fun loadBalance() {
        viewModelScope.launch {
            val userRes = authRepository.getCurrentUser()
            if (userRes is Resource.Success && userRes.data != null) {
                val res = creditsRepository.getBalance(userRes.data.id)
                if (res is Resource.Success) _balance.intValue = res.data ?: 0
            }
        }
    }

    /** Test-mode: add credits directly for development/demo purposes */
    fun simulateAddCredits(amount: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            val userRes = authRepository.getCurrentUser()
            if (userRes is Resource.Success && userRes.data != null) {
                when (val result = creditsRepository.addCredits(userRes.data.id, amount)) {
                    is Resource.Success -> {
                        _balance.intValue = result.data ?: _balance.intValue
                        _uiEvent.emit(UiEvent.ShowSnackbar("$amount credits added!"))
                    }
                    is Resource.Error -> _uiEvent.emit(UiEvent.ShowSnackbar(result.message ?: "Failed"))
                    else -> Unit
                }
            }
            _isLoading.value = false
        }
    }

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
    }
}
