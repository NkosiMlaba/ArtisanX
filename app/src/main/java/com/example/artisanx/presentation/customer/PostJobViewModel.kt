package com.example.artisanx.presentation.customer

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.artisanx.domain.repository.AiRepository
import com.example.artisanx.domain.repository.AuthRepository
import com.example.artisanx.domain.repository.JobRepository
import com.example.artisanx.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostJobViewModel @Inject constructor(
    private val jobRepository: JobRepository,
    private val authRepository: AuthRepository,
    private val aiRepository: AiRepository
) : ViewModel() {

    private val _title = mutableStateOf("")
    val title: State<String> = _title

    private val _description = mutableStateOf("")
    val description: State<String> = _description

    private val _category = mutableStateOf("")
    val category: State<String> = _category

    private val _address = mutableStateOf("")
    val address: State<String> = _address

    private val _latitude = mutableStateOf(0.0)
    private val _longitude = mutableStateOf(0.0)

    private val _budget = mutableStateOf("")
    val budget: State<String> = _budget

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    // AI state
    private val _isAiLoading = mutableStateOf(false)
    val isAiLoading: State<Boolean> = _isAiLoading

    private val _aiSuggestion = mutableStateOf<String?>(null)
    val aiSuggestion: State<String?> = _aiSuggestion

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun onTitleChange(value: String) { _title.value = value }
    fun onDescriptionChange(value: String) { _description.value = value }
    fun onCategoryChange(value: String) { _category.value = value }
    fun onAddressChange(value: String) { _address.value = value }
    fun onBudgetChange(value: String) { _budget.value = value }

    fun onLocationSelected(address: String, lat: Double, lng: Double) {
        _address.value = address
        _latitude.value = lat
        _longitude.value = lng
    }

    fun generateAiDescription() {
        if (_description.value.isBlank()) {
            viewModelScope.launch { _uiEvent.emit(UiEvent.ShowSnackbar("Type a rough description first")) }
            return
        }
        _isAiLoading.value = true
        viewModelScope.launch {
            when (val result = aiRepository.generateJobDescription(
                category = _category.value.ifBlank { "General" },
                roughDescription = _description.value
            )) {
                is Resource.Success -> _aiSuggestion.value = result.data
                is Resource.Error -> _uiEvent.emit(UiEvent.ShowSnackbar(result.message ?: "AI unavailable"))
                else -> Unit
            }
            _isAiLoading.value = false
        }
    }

    fun acceptAiSuggestion() {
        _aiSuggestion.value?.let { _description.value = it }
        _aiSuggestion.value = null
    }

    fun dismissAiSuggestion() {
        _aiSuggestion.value = null
    }

    fun submitJob() {
        val budgetVal = _budget.value.toDoubleOrNull()
        if (_title.value.isBlank() || _description.value.isBlank() || _category.value.isBlank() || budgetVal == null) {
            viewModelScope.launch { _uiEvent.emit(UiEvent.ShowSnackbar("Please fill all fields with valid data.")) }
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            val userRes = authRepository.getCurrentUser()
            if (userRes is Resource.Success) {
                val customerId = userRes.data?.id ?: ""
                val result = jobRepository.createJob(
                    customerId = customerId,
                    title = _title.value,
                    description = _description.value,
                    category = _category.value,
                    address = _address.value.ifBlank { "Not specified" },
                    budget = budgetVal,
                    latitude = _latitude.value,
                    longitude = _longitude.value
                )

                _isLoading.value = false
                when (result) {
                    is Resource.Success -> {
                        _uiEvent.emit(UiEvent.ShowSnackbar("Job posted successfully!"))
                        _uiEvent.emit(UiEvent.NavigateBack)
                    }
                    is Resource.Error -> _uiEvent.emit(UiEvent.ShowSnackbar(result.message ?: "Failed to post job"))
                    else -> Unit
                }
            } else {
                _isLoading.value = false
                _uiEvent.emit(UiEvent.ShowSnackbar("Could not get user session."))
            }
        }
    }

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
        object NavigateBack : UiEvent()
    }
}
