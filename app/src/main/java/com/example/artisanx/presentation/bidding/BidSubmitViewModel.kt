package com.example.artisanx.presentation.bidding

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.artisanx.domain.model.Job
import com.example.artisanx.domain.repository.AiRepository
import com.example.artisanx.domain.repository.AuthRepository
import com.example.artisanx.domain.repository.BiddingRepository
import com.example.artisanx.domain.repository.BidSuggestion
import com.example.artisanx.domain.repository.CreditsRepository
import com.example.artisanx.domain.repository.JobRepository
import com.example.artisanx.domain.repository.ProfileRepository
import com.example.artisanx.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BidSubmitViewModel @Inject constructor(
    private val biddingRepository: BiddingRepository,
    private val jobRepository: JobRepository,
    private val creditsRepository: CreditsRepository,
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
    private val aiRepository: AiRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val jobId = savedStateHandle.get<String>("jobId") ?: ""

    private val _job = mutableStateOf<Job?>(null)
    val job: State<Job?> = _job

    private val _priceOffer = mutableStateOf("")
    val priceOffer: State<String> = _priceOffer

    private val _message = mutableStateOf("")
    val message: State<String> = _message

    private val _estimatedHours = mutableStateOf("")
    val estimatedHours: State<String> = _estimatedHours

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _creditBalance = mutableStateOf(0)
    val creditBalance: State<Int> = _creditBalance

    // AI state
    private val _isAiLoading = mutableStateOf(false)
    val isAiLoading: State<Boolean> = _isAiLoading

    private val _aiSuggestion = mutableStateOf<BidSuggestion?>(null)
    val aiSuggestion: State<BidSuggestion?> = _aiSuggestion

    private val _artisanSkills = mutableStateOf("")
    private val _existingBidId = mutableStateOf<String?>(null)
    private val _isEditMode = mutableStateOf(false)
    val isEditMode: State<Boolean> = _isEditMode

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    init {
        loadJobAndCredits()
    }

    private fun loadJobAndCredits() {
        viewModelScope.launch {
            when (val result = jobRepository.getJobById(jobId)) {
                is Resource.Success -> _job.value = result.data
                is Resource.Error -> _uiEvent.emit(UiEvent.ShowSnackbar(result.message ?: "Failed to load job"))
                else -> Unit
            }

            val userRes = authRepository.getCurrentUser()
            if (userRes is Resource.Success && userRes.data != null) {
                val userId = userRes.data.id
                when (val creditsRes = creditsRepository.getBalance(userId)) {
                    is Resource.Success -> _creditBalance.value = creditsRes.data ?: 0
                    else -> Unit
                }
                // Load artisan's skills for AI context
                val profileRes = profileRepository.getArtisanProfile(userId)
                if (profileRes is Resource.Success) {
                    _artisanSkills.value = profileRes.data?.data?.get("skills") as? String ?: ""
                }

                // Check for existing bid to pre-populate in edit mode
                val existingBidRes = biddingRepository.getArtisanBidForJob(jobId, userId)
                if (existingBidRes is Resource.Success && existingBidRes.data != null) {
                    val existing = existingBidRes.data
                    if (existing.status == "pending") {
                        _existingBidId.value = existing.id
                        _isEditMode.value = true
                        _priceOffer.value = existing.priceOffer.toInt().toString()
                        _message.value = existing.message
                        _estimatedHours.value = existing.estimatedHours.toInt().toString()
                    }
                }
            }
        }
    }

    fun getAiSuggestion() {
        val job = _job.value ?: run {
            viewModelScope.launch { _uiEvent.emit(UiEvent.ShowSnackbar("Job not loaded yet")) }
            return
        }
        _isAiLoading.value = true
        viewModelScope.launch {
            when (val result = aiRepository.getBidSuggestion(
                jobTitle = job.title,
                jobDescription = job.description,
                category = job.category,
                budget = job.budget,
                artisanSkills = _artisanSkills.value
            )) {
                is Resource.Success -> _aiSuggestion.value = result.data
                is Resource.Error -> _uiEvent.emit(UiEvent.ShowSnackbar(result.message ?: "AI unavailable"))
                else -> Unit
            }
            _isAiLoading.value = false
        }
    }

    fun acceptAiSuggestion() {
        val suggestion = _aiSuggestion.value ?: return
        _priceOffer.value = suggestion.minPrice.toInt().toString()
        _message.value = suggestion.messageTemplate
        _aiSuggestion.value = null
    }

    fun dismissAiSuggestion() {
        _aiSuggestion.value = null
    }

    fun onPriceOfferChange(value: String) { _priceOffer.value = value }
    fun onMessageChange(value: String) { _message.value = value }
    fun onEstimatedHoursChange(value: String) { _estimatedHours.value = value }

    fun submitBid() {
        val price = _priceOffer.value.toDoubleOrNull()
        val hours = _estimatedHours.value.toDoubleOrNull()

        if (price == null || price <= 0) {
            viewModelScope.launch { _uiEvent.emit(UiEvent.ShowSnackbar("Enter a valid price")) }
            return
        }
        if (_message.value.length < 10) {
            viewModelScope.launch { _uiEvent.emit(UiEvent.ShowSnackbar("Message must be at least 10 characters")) }
            return
        }
        if (hours == null || hours <= 0) {
            viewModelScope.launch { _uiEvent.emit(UiEvent.ShowSnackbar("Enter valid estimated hours")) }
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            val userRes = authRepository.getCurrentUser()
            if (userRes !is Resource.Success || userRes.data == null) {
                _isLoading.value = false
                _uiEvent.emit(UiEvent.ShowSnackbar("Session expired. Please login again."))
                return@launch
            }
            val userId = userRes.data.id

            if (_isEditMode.value) {
                // Update existing bid — no credits deducted for edits
                val bidId = _existingBidId.value ?: return@launch
                when (val result = biddingRepository.updateBid(bidId, price, _message.value, hours)) {
                    is Resource.Success -> {
                        _uiEvent.emit(UiEvent.ShowSnackbar("Bid updated successfully"))
                        _uiEvent.emit(UiEvent.NavigateBack)
                    }
                    is Resource.Error -> _uiEvent.emit(UiEvent.ShowSnackbar(result.message ?: "Failed to update bid"))
                    else -> Unit
                }
            } else {
                // New bid — deduct credits and create
                if (biddingRepository.hasArtisanBid(jobId, userId)) {
                    _isLoading.value = false
                    _uiEvent.emit(UiEvent.ShowSnackbar("You've already bid on this job"))
                    return@launch
                }

                val deductRes = creditsRepository.deductCredits(userId, 2)
                if (deductRes is Resource.Error) {
                    _isLoading.value = false
                    _uiEvent.emit(UiEvent.ShowSnackbar(deductRes.message ?: "Insufficient credits"))
                    return@launch
                }

                when (val result = biddingRepository.submitBid(
                    jobId = jobId,
                    artisanId = userId,
                    priceOffer = price,
                    message = _message.value,
                    estimatedHours = hours
                )) {
                    is Resource.Success -> {
                        _uiEvent.emit(UiEvent.ShowSnackbar("Bid submitted! (2 credits used)"))
                        _uiEvent.emit(UiEvent.NavigateBack)
                    }
                    is Resource.Error -> _uiEvent.emit(UiEvent.ShowSnackbar(result.message ?: "Failed to submit bid"))
                    else -> Unit
                }
            }
            _isLoading.value = false
        }
    }

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
        object NavigateBack : UiEvent()
    }
}
