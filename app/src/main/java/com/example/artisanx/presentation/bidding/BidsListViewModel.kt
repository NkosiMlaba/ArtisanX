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
import com.example.artisanx.domain.repository.ProfileRepository
import com.example.artisanx.domain.repository.RatingStats
import com.example.artisanx.domain.repository.ReviewRepository
import com.example.artisanx.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BidWithArtisan(
    val bid: Bid,
    val artisanName: String = "",
    val artisanBadge: String = "",
    val artisanRating: Double = 0.0,
    val artisanReviewCount: Int = 0
)

@HiltViewModel
class BidsListViewModel @Inject constructor(
    private val biddingRepository: BiddingRepository,
    private val jobRepository: JobRepository,
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
    private val reviewRepository: ReviewRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val jobId = savedStateHandle.get<String>("jobId") ?: ""

    private val _job = mutableStateOf<Job?>(null)
    val job: State<Job?> = _job

    private val _bids = mutableStateOf<List<BidWithArtisan>>(emptyList())
    val bids: State<List<BidWithArtisan>> = _bids

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
                is Resource.Success -> {
                    val rawBids = bidsRes.data ?: emptyList()
                    // Enrich each bid with artisan profile info + live rating stats, in parallel
                    val enriched = coroutineScope {
                        rawBids.map { bid ->
                            async {
                                val profileDeferred = async { profileRepository.getArtisanProfile(bid.artisanId) }
                                val statsDeferred = async { reviewRepository.getArtisanRatingStats(bid.artisanId) }
                                val profile = (profileDeferred.await() as? Resource.Success)?.data?.data
                                val stats = (statsDeferred.await() as? Resource.Success)?.data ?: RatingStats.EMPTY
                                BidWithArtisan(
                                    bid = bid,
                                    artisanName = profile?.get("fullName") as? String ?: "Artisan",
                                    artisanBadge = profile?.get("badge") as? String ?: "",
                                    artisanRating = stats.avg,
                                    artisanReviewCount = stats.count
                                )
                            }
                        }.awaitAll()
                    }
                    _bids.value = enriched
                }
                is Resource.Error -> _uiEvent.emit(UiEvent.ShowSnackbar(bidsRes.message ?: "Failed to load bids"))
                else -> Unit
            }
            _isLoading.value = false
        }
    }

    fun acceptBid(bidWithArtisan: BidWithArtisan) {
        _isLoading.value = true
        viewModelScope.launch {
            val userRes = authRepository.getCurrentUser()
            val customerId = (userRes as? Resource.Success)?.data?.id ?: ""

            when (val result = biddingRepository.acceptBid(
                bidId = bidWithArtisan.bid.id,
                jobId = jobId,
                artisanId = bidWithArtisan.bid.artisanId,
                customerId = customerId
            )) {
                is Resource.Success -> {
                    _uiEvent.emit(UiEvent.ShowSnackbar("Bid accepted! Booking created."))
                    loadData()
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
