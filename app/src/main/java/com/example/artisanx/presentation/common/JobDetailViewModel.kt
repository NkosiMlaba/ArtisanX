package com.example.artisanx.presentation.common

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.artisanx.data.local.DataStoreManager
import com.example.artisanx.domain.model.Job
import com.example.artisanx.domain.repository.AiRepository
import com.example.artisanx.domain.repository.ArtisanMatch
import com.example.artisanx.domain.repository.ArtisanSummary
import com.example.artisanx.domain.repository.AuthRepository
import com.example.artisanx.domain.repository.BiddingRepository
import com.example.artisanx.domain.repository.JobRepository
import com.example.artisanx.domain.repository.ProfileRepository
import com.example.artisanx.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JobDetailViewModel @Inject constructor(
    private val jobRepository: JobRepository,
    private val authRepository: AuthRepository,
    private val biddingRepository: BiddingRepository,
    private val profileRepository: ProfileRepository,
    private val aiRepository: AiRepository,
    private val dataStoreManager: DataStoreManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val jobId = savedStateHandle.get<String>("jobId") ?: ""

    private val _job = mutableStateOf<Job?>(null)
    val job: State<Job?> = _job

    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

    private val _suggestedArtisans = mutableStateOf<List<ArtisanMatch>>(emptyList())
    val suggestedArtisans: State<List<ArtisanMatch>> = _suggestedArtisans

    private val _isMatchLoading = mutableStateOf(false)
    val isMatchLoading: State<Boolean> = _isMatchLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    private val _userRole = mutableStateOf<String?>(null)
    val userRole: State<String?> = _userRole

    private val _isOwnJob = mutableStateOf(false)
    val isOwnJob: State<Boolean> = _isOwnJob

    private val _hasAlreadyBid = mutableStateOf(false)
    val hasAlreadyBid: State<Boolean> = _hasAlreadyBid

    private val _bidCount = mutableStateOf(0)
    val bidCount: State<Int> = _bidCount

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
            // Load role
            _userRole.value = dataStoreManager.userRoleFlow.first()

            // Load job
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

            // Check ownership and bid status
            val userRes = authRepository.getCurrentUser()
            if (userRes is Resource.Success && userRes.data != null) {
                val userId = userRes.data.id
                _isOwnJob.value = _job.value?.customerId == userId

                if (_userRole.value == "artisan") {
                    _hasAlreadyBid.value = biddingRepository.hasArtisanBid(jobId, userId)
                }

                // Load bid count for job owner
                if (_isOwnJob.value) {
                    val bidsRes = biddingRepository.getBidsForJob(jobId)
                    if (bidsRes is Resource.Success) {
                        _bidCount.value = bidsRes.data?.size ?: 0
                    }
                }
            }

            _isLoading.value = false

            // Load AI artisan matches for job owner on open jobs (runs after main load)
            val job = _job.value
            if (_isOwnJob.value && job?.status == "open") {
                loadArtisanMatches(job)
            }
        }
    }

    private fun loadArtisanMatches(job: Job) {
        _isMatchLoading.value = true
        viewModelScope.launch {
            try {
                // Fetch artisans in the same trade category
                val artisanDocsRes = profileRepository.getArtisansByCategory(job.category)
                if (artisanDocsRes is Resource.Success) {
                    val artisanSummaries = artisanDocsRes.data ?: emptyList()
                    if (artisanSummaries.isNotEmpty()) {
                        when (val matchRes = aiRepository.matchArtisans(
                            jobTitle = job.title,
                            jobDescription = job.description,
                            category = job.category,
                            artisans = artisanSummaries
                        )) {
                            is Resource.Success -> _suggestedArtisans.value = matchRes.data ?: emptyList()
                            else -> Unit
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            _isMatchLoading.value = false
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
