package com.example.artisanx.presentation.review

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.artisanx.domain.model.Review
import com.example.artisanx.domain.repository.AuthRepository
import com.example.artisanx.domain.repository.ReviewRepository
import com.example.artisanx.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val reviewRepository: ReviewRepository,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val bookingId = savedStateHandle.get<String>("bookingId") ?: ""
    private val artisanId = savedStateHandle.get<String>("artisanId") ?: ""

    private val _rating = mutableIntStateOf(0)
    val rating: State<Int> = _rating

    private val _comment = mutableStateOf("")
    val comment: State<String> = _comment

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _existingReview = mutableStateOf<Review?>(null)
    val existingReview: State<Review?> = _existingReview

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    init {
        checkExistingReview()
    }

    private fun checkExistingReview() {
        viewModelScope.launch {
            val result = reviewRepository.getReviewForBooking(bookingId)
            if (result is Resource.Success && result.data != null) {
                _existingReview.value = result.data
                _rating.intValue = result.data.rating
                _comment.value = result.data.comment
            }
        }
    }

    fun onRatingChange(value: Int) { _rating.intValue = value }
    fun onCommentChange(value: String) { _comment.value = value }

    fun submitReview() {
        if (_rating.intValue == 0) {
            viewModelScope.launch { _uiEvent.emit(UiEvent.ShowSnackbar("Please select a star rating")) }
            return
        }
        if (_comment.value.isBlank()) {
            viewModelScope.launch { _uiEvent.emit(UiEvent.ShowSnackbar("Please write a comment")) }
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            val userRes = authRepository.getCurrentUser()
            if (userRes !is Resource.Success || userRes.data == null) {
                _uiEvent.emit(UiEvent.ShowSnackbar("Session error"))
                _isLoading.value = false
                return@launch
            }

            when (val result = reviewRepository.submitReview(
                bookingId = bookingId,
                customerId = userRes.data.id,
                artisanId = artisanId,
                rating = _rating.intValue,
                comment = _comment.value
            )) {
                is Resource.Success -> {
                    _uiEvent.emit(UiEvent.ShowSnackbar("Review submitted! Thank you."))
                    _uiEvent.emit(UiEvent.NavigateBack)
                }
                is Resource.Error -> {
                    _uiEvent.emit(UiEvent.ShowSnackbar(result.message ?: "Failed to submit review"))
                }
                else -> Unit
            }
            _isLoading.value = false
        }
    }

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
        object NavigateBack : UiEvent()
    }
}
