package com.example.artisanx.presentation.chat

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.artisanx.data.local.DataStoreManager
import com.example.artisanx.domain.repository.AuthRepository
import com.example.artisanx.domain.repository.BookingRepository
import com.example.artisanx.domain.repository.ChatRepository
import com.example.artisanx.domain.repository.JobRepository
import com.example.artisanx.domain.repository.ProfileRepository
import com.example.artisanx.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatPreview(
    val bookingId: String,
    val jobTitle: String,
    val otherPartyName: String,
    val lastMessageText: String,
    val lastMessageTime: String
)

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val chatRepository: ChatRepository,
    private val jobRepository: JobRepository,
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val _previews = mutableStateOf<List<ChatPreview>>(emptyList())
    val previews: State<List<ChatPreview>> = _previews

    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

    init {
        loadChats()
    }

    fun loadChats() {
        _isLoading.value = true
        viewModelScope.launch {
            val userRes = authRepository.getCurrentUser()
            if (userRes !is Resource.Success || userRes.data == null) {
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

            if (bookingsRes !is Resource.Success) {
                _isLoading.value = false
                return@launch
            }

            val bookings = bookingsRes.data ?: emptyList()
            // Only show bookings where chat is accessible (accepted onwards)
            val chatableBookings = bookings.filter { it.status in listOf("accepted", "in_progress", "completed") }

            val chatPreviews = chatableBookings.mapNotNull { booking ->
                val messagesRes = chatRepository.getMessages(booking.id)
                val messages = (messagesRes as? Resource.Success)?.data ?: emptyList()

                val job = (jobRepository.getJobById(booking.jobId) as? Resource.Success)?.data
                val jobTitle = job?.title ?: "Booking"

                val otherUserId = if (isArtisan) booking.customerId else booking.artisanId
                val otherName = if (isArtisan) {
                    val res = profileRepository.getUserProfile(otherUserId)
                    (res as? Resource.Success)?.data?.data?.get("fullName") as? String ?: "Customer"
                } else {
                    val res = profileRepository.getArtisanProfile(otherUserId)
                    (res as? Resource.Success)?.data?.data?.get("fullName") as? String ?: "Artisan"
                }

                val lastMsg = messages.lastOrNull()
                ChatPreview(
                    bookingId = booking.id,
                    jobTitle = jobTitle,
                    otherPartyName = otherName,
                    lastMessageText = lastMsg?.message ?: "No messages yet",
                    lastMessageTime = lastMsg?.createdAt?.take(10) ?: ""
                )
            }

            // Sort by most recent message first (lastMessageTime desc)
            _previews.value = chatPreviews.sortedByDescending { it.lastMessageTime }
            _isLoading.value = false
        }
    }
}
