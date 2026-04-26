package com.example.artisanx.presentation.chat

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.artisanx.ArtisansXFirebaseService
import com.example.artisanx.domain.model.ChatMessage
import com.example.artisanx.domain.repository.AuthRepository
import com.example.artisanx.domain.repository.BookingRepository
import com.example.artisanx.domain.repository.ChatRepository
import com.example.artisanx.domain.repository.ProfileRepository
import com.example.artisanx.util.AppwriteFileUtils
import com.example.artisanx.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.appwrite.services.Storage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val bookingRepository: BookingRepository,
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
    private val storage: Storage,
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val bookingId = savedStateHandle.get<String>("bookingId") ?: ""

    private val _messages = mutableStateOf<List<ChatMessage>>(emptyList())
    val messages: State<List<ChatMessage>> = _messages

    private val _messageInput = mutableStateOf("")
    val messageInput: State<String> = _messageInput

    private val _isSending = mutableStateOf(false)
    val isSending: State<Boolean> = _isSending

    private val _currentUserId = mutableStateOf("")
    val currentUserId: State<String> = _currentUserId

    private val _otherPartyName = mutableStateOf("Chat")
    val otherPartyName: State<String> = _otherPartyName

    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

    private val _isImageUploading = mutableStateOf(false)
    val isImageUploading: State<Boolean> = _isImageUploading

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private var pollJob: Job? = null

    init {
        loadUserAndContext()
        startPolling()
    }

    private fun loadUserAndContext() {
        viewModelScope.launch {
            val userRes = authRepository.getCurrentUser()
            if (userRes !is Resource.Success || userRes.data == null) return@launch
            val userId = userRes.data.id
            _currentUserId.value = userId

            // Load booking to find the other party
            val bookingRes = bookingRepository.getBookingById(bookingId)
            if (bookingRes is Resource.Success && bookingRes.data != null) {
                val booking = bookingRes.data
                val otherUserId = if (booking.artisanId == userId) booking.customerId else booking.artisanId
                val isOtherArtisan = booking.artisanId != userId

                // Fetch the other party's name from their profile
                val name = if (isOtherArtisan) {
                    val profileRes = profileRepository.getArtisanProfile(otherUserId)
                    if (profileRes is Resource.Success) {
                        profileRes.data?.data?.get("fullName") as? String ?: "Artisan"
                    } else "Artisan"
                } else {
                    val profileRes = profileRepository.getUserProfile(otherUserId)
                    if (profileRes is Resource.Success) {
                        profileRes.data?.data?.get("fullName") as? String ?: "Customer"
                    } else "Customer"
                }
                _otherPartyName.value = name
            }

            refreshMessages()
            _isLoading.value = false
        }
    }

    private fun startPolling() {
        pollJob = viewModelScope.launch {
            while (isActive) {
                delay(5_000)
                refreshMessages()
            }
        }
    }

    private suspend fun refreshMessages() {
        if (bookingId.isBlank()) return
        when (val result = chatRepository.getMessages(bookingId)) {
            is Resource.Success -> {
                val newMessages = result.data ?: emptyList()
                val previousIds = _messages.value.map { it.id }.toSet()
                val currentId = _currentUserId.value
                // Notify about new messages from the other party (not our own sends)
                if (currentId.isNotBlank() && previousIds.isNotEmpty()) {
                    newMessages
                        .filter { it.id !in previousIds && it.senderId != currentId }
                        .forEach { msg ->
                            ArtisansXFirebaseService.showLocalNotification(
                                context,
                                _otherPartyName.value.ifBlank { "New Message" },
                                msg.message.take(80),
                                bookingId
                            )
                        }
                }
                _messages.value = newMessages
            }
            else -> Unit
        }
    }

    fun onInputChange(value: String) {
        _messageInput.value = value
    }

    fun sendImageMessage(uri: Uri) {
        if (_currentUserId.value.isBlank()) return
        _isImageUploading.value = true
        viewModelScope.launch {
            val fileId = AppwriteFileUtils.uploadFromUri(context, storage, uri, "chat_img")
            if (fileId != null) {
                when (val result = chatRepository.sendImageMessage(bookingId, _currentUserId.value, fileId)) {
                    is Resource.Success -> refreshMessages()
                    is Resource.Error -> _uiEvent.emit(UiEvent.ShowSnackbar(result.message ?: "Failed to send image"))
                    else -> Unit
                }
            } else {
                _uiEvent.emit(UiEvent.ShowSnackbar("Failed to upload image"))
            }
            _isImageUploading.value = false
        }
    }

    fun sendMessage() {
        val text = _messageInput.value.trim()
        if (text.isBlank() || _currentUserId.value.isBlank()) return
        if (text.length > 2000) {
            viewModelScope.launch { _uiEvent.emit(UiEvent.ShowSnackbar("Message too long (max 2000 chars)")) }
            return
        }

        _isSending.value = true
        viewModelScope.launch {
            when (val result = chatRepository.sendMessage(bookingId, _currentUserId.value, text)) {
                is Resource.Success -> {
                    _messageInput.value = ""
                    refreshMessages()
                }
                is Resource.Error -> {
                    _uiEvent.emit(UiEvent.ShowSnackbar(result.message ?: "Failed to send"))
                }
                else -> Unit
            }
            _isSending.value = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        pollJob?.cancel()
    }

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
    }
}
