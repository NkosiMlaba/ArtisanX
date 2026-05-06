package com.example.artisanx.presentation.reviews

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.artisanx.domain.model.Job
import com.example.artisanx.domain.model.Review
import com.example.artisanx.domain.repository.AuthRepository
import com.example.artisanx.domain.repository.BookingRepository
import com.example.artisanx.domain.repository.JobRepository
import com.example.artisanx.domain.repository.ProfileRepository
import com.example.artisanx.domain.repository.ReviewRepository
import com.example.artisanx.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReviewDetailState(
    val review: Review,
    val job: Job?,
    val otherPartyName: String,
    val otherPartyBadge: String,
    val otherPartyVerified: Boolean,
    val currentUserIsCustomer: Boolean
)

@HiltViewModel
class ReviewDetailViewModel @Inject constructor(
    private val reviewRepository: ReviewRepository,
    private val bookingRepository: BookingRepository,
    private val jobRepository: JobRepository,
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val reviewId: String = savedStateHandle.get<String>("reviewId") ?: ""

    private val _state = mutableStateOf<ReviewDetailState?>(null)
    val state: State<ReviewDetailState?> = _state

    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    init {
        load()
    }

    private fun load() {
        if (reviewId.isBlank()) {
            _error.value = "Invalid review id"
            _isLoading.value = false
            return
        }
        viewModelScope.launch {
            val userRes = authRepository.getCurrentUser()
            val userId = (userRes as? Resource.Success)?.data?.id

            val reviewRes = reviewRepository.getReviewById(reviewId)
            val review = (reviewRes as? Resource.Success)?.data
            if (review == null) {
                _error.value = (reviewRes as? Resource.Error)?.message ?: "Could not load review"
                _isLoading.value = false
                return@launch
            }

            val currentUserIsCustomer = review.customerId == userId
            val otherId = if (currentUserIsCustomer) review.artisanId else review.customerId

            val job = (bookingRepository.getBookingById(review.bookingId) as? Resource.Success)?.data
                ?.let { booking ->
                    (jobRepository.getJobById(booking.jobId) as? Resource.Success)?.data
                }

            val (otherName, otherBadge, otherVerified) = if (currentUserIsCustomer) {
                val data = (profileRepository.getArtisanProfile(otherId) as? Resource.Success)?.data?.data
                Triple(
                    data?.get("fullName") as? String ?: "Artisan",
                    data?.get("badge") as? String ?: "",
                    (data?.get("verified") as? Boolean) == true ||
                        (data?.get("badge") as? String).equals("Verified Artisan", ignoreCase = true)
                )
            } else {
                val data = (profileRepository.getUserProfile(otherId) as? Resource.Success)?.data?.data
                Triple(data?.get("fullName") as? String ?: "Customer", "", false)
            }

            _state.value = ReviewDetailState(
                review = review,
                job = job,
                otherPartyName = otherName,
                otherPartyBadge = otherBadge,
                otherPartyVerified = otherVerified,
                currentUserIsCustomer = currentUserIsCustomer
            )
            _isLoading.value = false
        }
    }
}
