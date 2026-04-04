package com.example.artisanx.presentation.bidding

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.artisanx.domain.model.Bid
import com.example.artisanx.domain.model.Job
import com.example.artisanx.domain.repository.AuthRepository
import com.example.artisanx.domain.repository.BiddingRepository
import com.example.artisanx.domain.repository.JobRepository
import com.example.artisanx.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BidsListViewModel @Inject constructor(
    private val biddingRepository: BiddingRepository,
    private val jobRepository: JobRepository,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val jobId = savedStateHandle.get<String>("jobId") ?: ""

    private val _job = mutableStateOf<Job?>(null)
    val job: State<Job?> = _job

    private val _bids = mutableStateOf<List<Bid>>(emptyList())
    val bids: State<List<Bid>> = _bids

    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    init {
        loadData()
    }

    fun loadData() {
        _isLoading.value = true
        viewModelScope.launch {
            when (val jobRes = jobRepository.getJobById(jobId)) {
                is Resource.Success -> _job.value = jobRes.data
                is Resource.Error -> _uiEvent.emit(UiEvent.ShowSnackbar(jobRes.message ?: "Failed to load job"))
                else -> Unit
            }

            when (val bidsRes = biddingRepository.getBidsForJob(jobId)) {
                is Resource.Success -> _bids.value = bidsRes.data ?: emptyList()
                is Resource.Error -> _uiEvent.emit(UiEvent.ShowSnackbar(bidsRes.message ?: "Failed to load bids"))
                else -> Unit
            }
            _isLoading.value = false
        }
    }

    fun acceptBid(bid: Bid) {
        _isLoading.value = true
        viewModelScope.launch {
            val userRes = authRepository.getCurrentUser()
            val customerId = (userRes as? Resource.Success)?.data?.id ?: ""

            when (val result = biddingRepository.acceptBid(
                bidId = bid.id,
                jobId = jobId,
                artisanId = bid.artisanId,
                customerId = customerId
            )) {
                is Resource.Success -> {
                    _uiEvent.emit(UiEvent.ShowSnackbar("Bid accepted! Booking created."))
                    loadData() // Refresh
                }
                is Resource.Error -> {
                    _uiEvent.emit(UiEvent.ShowSnackbar(result.message ?: "Failed to accept bid"))
                }
                else -> Unit
            }
            _isLoading.value = false
        }
    }

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
    }
}
