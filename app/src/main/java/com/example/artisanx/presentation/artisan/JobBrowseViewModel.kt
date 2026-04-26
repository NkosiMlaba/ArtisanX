package com.example.artisanx.presentation.artisan

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.artisanx.domain.model.Job
import com.example.artisanx.domain.repository.JobRepository
import com.example.artisanx.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class JobSortOption(val label: String) {
    NEWEST("Newest"),
    BUDGET_HIGH("Budget: High to Low"),
    URGENT_FIRST("Urgent First")
}

@HiltViewModel
class JobBrowseViewModel @Inject constructor(
    private val jobRepository: JobRepository
) : ViewModel() {

    private val _allJobs = mutableStateOf<List<Job>>(emptyList())

    private val _jobs = mutableStateOf<List<Job>>(emptyList())
    val jobs: State<List<Job>> = _jobs

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    private val _selectedCategory = mutableStateOf<String?>(null)
    val selectedCategory: State<String?> = _selectedCategory

    private val _selectedSort = mutableStateOf(JobSortOption.NEWEST)
    val selectedSort: State<JobSortOption> = _selectedSort

    init {
        loadJobs()
    }

    fun loadJobs() {
        _isLoading.value = true
        viewModelScope.launch {
            when (val result = jobRepository.getOpenJobs(_selectedCategory.value)) {
                is Resource.Success -> {
                    _allJobs.value = result.data ?: emptyList()
                    applySort()
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

    fun setCategoryFilter(category: String?) {
        _selectedCategory.value = category
        loadJobs()
    }

    fun setSortOption(option: JobSortOption) {
        _selectedSort.value = option
        applySort()
    }

    private fun applySort() {
        _jobs.value = when (_selectedSort.value) {
            JobSortOption.NEWEST -> _allJobs.value.sortedByDescending { it.createdAt }
            JobSortOption.BUDGET_HIGH -> _allJobs.value.sortedByDescending { it.budget }
            JobSortOption.URGENT_FIRST -> _allJobs.value.sortedWith(
                compareByDescending<Job> { it.urgency == "urgent" }.thenByDescending { it.createdAt }
            )
        }
    }
}
