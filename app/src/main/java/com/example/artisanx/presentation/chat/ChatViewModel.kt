package com.example.artisanx.presentation.chat

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.artisanx.domain.model.ChatMessage
import com.example.artisanx.domain.repository.AuthRepository
import com.example.artisanx.domain.repository.BookingRepository
import com.example.artisanx.domain.repository.ChatRepository
import com.example.artisanx.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
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

    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private var pollJob: Job? = null

    init {
        loadUserAndMessages()
        startPolling()
    }

    private fun loadUserAndMessages() {
        viewModelScope.launch {
            val userRes = authRepository.getCurrentUser()
            if (userRes is Resource.Success && userRes.data != null) {
                _currentUserId.value = userRes.data.id
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
            is Resource.Success -> _messages.value = result.data ?: emptyList()
            else -> Unit
        }
    }

    fun onInputChange(value: String) {
        _messageInput.value = value
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
