package com.example.artisanx.presentation.booking

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.artisanx.data.local.DataStoreManager
import com.example.artisanx.domain.model.Booking
import com.example.artisanx.domain.model.Job
import com.example.artisanx.domain.repository.AuthRepository
import com.example.artisanx.domain.repository.BookingRepository
import com.example.artisanx.domain.repository.JobRepository
import com.example.artisanx.domain.repository.ProfileRepository
import com.example.artisanx.domain.repository.ReviewRepository
import com.example.artisanx.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BookingWithJob(
    val booking: Booking,
    val job: Job?,
    val otherPartyName: String = "",
    val isReviewed: Boolean = false
)

@HiltViewModel
class BookingsViewModel @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val jobRepository: JobRepository,
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
    private val reviewRepository: ReviewRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val _bookings = mutableStateOf<List<BookingWithJob>>(emptyList())
    val bookings: State<List<BookingWithJob>> = _bookings

    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    init {
        loadBookings()
    }

    fun loadBookings() {
        _isLoading.value = true
        viewModelScope.launch {
            val userRes = authRepository.getCurrentUser()
            if (userRes !is Resource.Success || userRes.data == null) {
                _error.value = "Could not load user"
                _isLoading.value = false
                return@launch
            }

            val userId = userRes.data.id
            val role = dataStoreManager.userRoleFlow.first() ?: "customer"
            val isArtisan = role == "artisan"

            val bookingsRes = if (isArtisan) {
                bookingRepository.getBookingsForArtisan(userId)
            } else {
                bookingRepository.getBookingsForCustomer(userId)
            }

            when (bookingsRes) {
                is Resource.Success -> {
                    val bookingsList = bookingsRes.data ?: emptyList()
                    val enriched = bookingsList.map { booking ->
                        // Load the linked job
                        val job = (jobRepository.getJobById(booking.jobId) as? Resource.Success)?.data

                        // Load the other party's name
                        val otherUserId = if (isArtisan) booking.customerId else booking.artisanId
                        val otherName = if (isArtisan) {
                            // Other party is the customer — load from user_profiles
                            val profileRes = profileRepository.getUserProfile(otherUserId)
                            (profileRes as? Resource.Success)?.data?.data?.get("fullName") as? String ?: "Customer"
                        } else {
                            // Other party is the artisan — load from artisan_profiles
                            val profileRes = profileRepository.getArtisanProfile(otherUserId)
                            (profileRes as? Resource.Success)?.data?.data?.get("fullName") as? String ?: "Artisan"
                        }

                        // Check if this booking already has a review (only relevant for customer on completed bookings)
                        val isReviewed = if (!isArtisan && booking.status == "completed") {
                            val reviewRes = reviewRepository.getReviewForBooking(booking.id)
                            (reviewRes as? Resource.Success)?.data != null
                        } else false

                        BookingWithJob(
                            booking = booking,
                            job = job,
                            otherPartyName = otherName,
                            isReviewed = isReviewed
                        )
                    }
                    _bookings.value = enriched
                    _error.value = null
                }
                is Resource.Error -> _error.value = bookingsRes.message
                else -> Unit
            }
            _isLoading.value = false
        }
    }

    fun updateStatus(bookingId: String, newStatus: String) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = bookingRepository.updateBookingStatus(bookingId, newStatus)) {
                is Resource.Success -> {
                    _uiEvent.emit(UiEvent.ShowSnackbar("Status updated to ${newStatus.replace("_", " ")}"))
                    loadBookings()
                }
                is Resource.Error -> {
                    _uiEvent.emit(UiEvent.ShowSnackbar(result.message ?: "Failed to update status"))
                    _isLoading.value = false
                }
                else -> Unit
            }
        }
    }

    fun markAsPaid(bookingId: String) {
        viewModelScope.launch {
            when (val result = bookingRepository.markAsPaid(bookingId)) {
                is Resource.Success -> {
                    _uiEvent.emit(UiEvent.ShowSnackbar("Marked as paid"))
                    loadBookings()
                }
                is Resource.Error -> {
                    _uiEvent.emit(UiEvent.ShowSnackbar(result.message ?: "Failed to mark as paid"))
                }
                else -> Unit
            }
        }
    }

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
    }
}
