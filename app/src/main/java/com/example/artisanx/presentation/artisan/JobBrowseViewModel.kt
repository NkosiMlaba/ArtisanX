package com.example.artisanx.presentation.artisan

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.artisanx.domain.model.Job
import com.example.artisanx.domain.repository.AuthRepository
import com.example.artisanx.domain.repository.JobRepository
import com.example.artisanx.domain.repository.ProfileRepository
import com.example.artisanx.util.LocationUtils
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
    private val jobRepository: JobRepository,
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private var artisanLat: Double = 0.0
    private var artisanLng: Double = 0.0

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

    private val _searchQuery = mutableStateOf("")
    val searchQuery: State<String> = _searchQuery

    init {
        loadArtisanLocation()
        loadJobs()
    }

    private fun loadArtisanLocation() {
        viewModelScope.launch {
            val userRes = authRepository.getCurrentUser()
            if (userRes !is Resource.Success || userRes.data == null) return@launch
            val profileRes = profileRepository.getArtisanProfile(userRes.data.id)
            if (profileRes is Resource.Success && profileRes.data != null) {
                val data = profileRes.data.data
                artisanLat = (data["latitude"] as? Number)?.toDouble() ?: 0.0
                artisanLng = (data["longitude"] as? Number)?.toDouble() ?: 0.0
            }
        }
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

    fun distanceKmFor(job: Job): Double? {
        if (artisanLat == 0.0 && artisanLng == 0.0) return null
        if (job.latitude == 0.0 && job.longitude == 0.0) return null
        return LocationUtils.haversineKm(artisanLat, artisanLng, job.latitude, job.longitude)
    }

    fun setSortOption(option: JobSortOption) {
        _selectedSort.value = option
        applySort()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        applySort()
    }

    private fun applySort() {
        val q = _searchQuery.value.trim().lowercase()
        val source = if (q.isBlank()) _allJobs.value else _allJobs.value.filter { job ->
            job.title.lowercase().contains(q) ||
            job.description.lowercase().contains(q) ||
            job.category.lowercase().contains(q) ||
            job.address.lowercase().contains(q)
        }
        _jobs.value = when (_selectedSort.value) {
            JobSortOption.NEWEST -> source.sortedByDescending { it.createdAt }
            JobSortOption.BUDGET_HIGH -> source.sortedByDescending { it.budget }
            JobSortOption.URGENT_FIRST -> source.sortedWith(
                compareByDescending<Job> { it.urgency == "urgent" }.thenByDescending { it.createdAt }
            )
        }
    }
}
