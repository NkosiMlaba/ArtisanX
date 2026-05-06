package com.example.artisanx.presentation.reviews

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.artisanx.domain.model.Review
import com.example.artisanx.domain.repository.AuthRepository
import com.example.artisanx.domain.repository.BookingRepository
import com.example.artisanx.domain.repository.JobRepository
import com.example.artisanx.domain.repository.ProfileRepository
import com.example.artisanx.domain.repository.ReviewRepository
import com.example.artisanx.presentation.navigation.Screen
import com.example.artisanx.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReviewWithContext(
    val review: Review,
    val otherPartyName: String,
    val jobTitle: String,
    val jobCategory: String
)

@HiltViewModel
class ReviewsListViewModel @Inject constructor(
    private val reviewRepository: ReviewRepository,
    private val authRepository: AuthRepository,
    private val bookingRepository: BookingRepository,
    private val jobRepository: JobRepository,
    private val profileRepository: ProfileRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val mode: String = savedStateHandle.get<String>("mode") ?: Screen.ReviewsList.MODE_RECEIVED

    private val _reviews = mutableStateOf<List<ReviewWithContext>>(emptyList())
    val reviews: State<List<ReviewWithContext>> = _reviews

    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    init {
        load()
    }

    fun load() {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            val userRes = authRepository.getCurrentUser()
            val userId = (userRes as? Resource.Success)?.data?.id
            if (userId.isNullOrBlank()) {
                _error.value = "Not signed in"
                _isLoading.value = false
                return@launch
            }

            val reviewsRes = if (mode == Screen.ReviewsList.MODE_GIVEN) {
                reviewRepository.getReviewsByCustomer(userId)
            } else {
                reviewRepository.getReviewsForArtisan(userId)
            }

            when (reviewsRes) {
                is Resource.Success -> {
                    val raw = reviewsRes.data ?: emptyList()
                    _reviews.value = enrich(raw, mode == Screen.ReviewsList.MODE_GIVEN)
                }
                is Resource.Error -> _error.value = reviewsRes.message ?: "Could not load reviews"
                else -> Unit
            }
            _isLoading.value = false
        }
    }

    private suspend fun enrich(
        raw: List<Review>,
        currentUserIsCustomer: Boolean
    ): List<ReviewWithContext> = coroutineScope {
        raw.map { review ->
            async {
                val otherId = if (currentUserIsCustomer) review.artisanId else review.customerId
                val nameDeferred = async {
                    if (currentUserIsCustomer) {
                        (profileRepository.getArtisanProfile(otherId) as? Resource.Success)
                            ?.data?.data?.get("fullName") as? String ?: "Artisan"
                    } else {
                        (profileRepository.getUserProfile(otherId) as? Resource.Success)
                            ?.data?.data?.get("fullName") as? String ?: "Customer"
                    }
                }
                val jobDeferred = async {
                    val booking = (bookingRepository.getBookingById(review.bookingId) as? Resource.Success)?.data
                    val jobId = booking?.jobId
                    if (jobId.isNullOrBlank()) Pair("Job", "")
                    else {
                        val job = (jobRepository.getJobById(jobId) as? Resource.Success)?.data
                        Pair(job?.title ?: "Job", job?.category ?: "")
                    }
                }
                val (jobTitle, jobCategory) = jobDeferred.await()
                ReviewWithContext(
                    review = review,
                    otherPartyName = nameDeferred.await(),
                    jobTitle = jobTitle,
                    jobCategory = jobCategory
                )
            }
        }.awaitAll()
    }
}
