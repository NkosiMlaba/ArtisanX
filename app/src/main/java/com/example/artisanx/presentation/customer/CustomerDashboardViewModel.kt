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

    private val _allJobs = mutableStateOf<List<Job>>(emptyList())

    private val _jobs = mutableStateOf<List<Job>>(emptyList())
    val jobs: State<List<Job>> = _jobs

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    private val _searchQuery = mutableStateOf("")
    val searchQuery: State<String> = _searchQuery

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
                        _allJobs.value = jobsRes.data ?: emptyList()
                        applyFilter()
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

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        applyFilter()
    }

    private fun applyFilter() {
        val q = _searchQuery.value.trim().lowercase()
        _jobs.value = if (q.isBlank()) _allJobs.value
        else _allJobs.value.filter { job ->
            job.title.lowercase().contains(q) ||
                job.description.lowercase().contains(q) ||
                job.category.lowercase().contains(q) ||
                job.address.lowercase().contains(q)
        }
    }
}
