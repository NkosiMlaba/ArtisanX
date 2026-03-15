package com.example.artisanx.presentation.common

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.artisanx.domain.model.Job
import com.example.artisanx.domain.repository.AuthRepository
import com.example.artisanx.domain.repository.JobRepository
import com.example.artisanx.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JobDetailViewModel @Inject constructor(
    private val jobRepository: JobRepository,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val jobId = savedStateHandle.get<String>("jobId") ?: ""

    private val _job = mutableStateOf<Job?>(null)
    val job: State<Job?> = _job

    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    init {
        loadJob()
    }

    private fun loadJob() {
        if (jobId.isBlank()) {
            _error.value = "Invalid Job ID"
            _isLoading.value = false
            return
        }
        
        _isLoading.value = true
        viewModelScope.launch {
            when (val result = jobRepository.getJobById(jobId)) {
                is Resource.Success -> {
                    _job.value = result.data
                    _error.value = null
                }
                is Resource.Error -> {
                    _error.value = result.message
                }
                else -> Unit
            }
            _isLoading.value = false
        }
    }

    fun deleteJob() {
        if (jobId.isBlank()) return
        
        _isLoading.value = true
        viewModelScope.launch {
            when (val result = jobRepository.deleteJob(jobId)) {
                is Resource.Success -> {
                    _uiEvent.emit(UiEvent.ShowSnackbar("Job deleted successfully"))
                    _uiEvent.emit(UiEvent.NavigateBack)
                }
                is Resource.Error -> {
                    _isLoading.value = false
                    _uiEvent.emit(UiEvent.ShowSnackbar(result.message ?: "Failed to delete job"))
                }
                else -> Unit
            }
        }
    }

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
        object NavigateBack : UiEvent()
    }
}
