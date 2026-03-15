package com.example.artisanx.presentation.customer

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.artisanx.domain.model.Job
import com.example.artisanx.domain.repository.AuthRepository
import com.example.artisanx.domain.repository.JobRepository
import com.example.artisanx.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomerDashboardViewModel @Inject constructor(
    private val jobRepository: JobRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _jobs = mutableStateOf<List<Job>>(emptyList())
    val jobs: State<List<Job>> = _jobs

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading
    
    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    init {
        loadMyJobs()
    }

    fun loadMyJobs() {
        _isLoading.value = true
        viewModelScope.launch {
            val userRes = authRepository.getCurrentUser()
            if (userRes is Resource.Success && userRes.data != null) {
                when (val jobsRes = jobRepository.getJobsByCustomer(userRes.data.id)) {
                    is Resource.Success -> {
                        _jobs.value = jobsRes.data ?: emptyList()
                        _error.value = null
                    }
                    is Resource.Error -> {
                        _error.value = jobsRes.message
                    }
                    else -> Unit
                }
            } else {
                _error.value = "User not logged in"
            }
            _isLoading.value = false
        }
    }
}
