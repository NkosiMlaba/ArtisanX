package com.example.artisanx.presentation.profile

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.artisanx.data.local.DataStoreManager
import com.example.artisanx.domain.model.Review
import com.example.artisanx.domain.repository.AuthRepository
import com.example.artisanx.domain.repository.ProfileRepository
import com.example.artisanx.domain.repository.ReviewRepository
import com.example.artisanx.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import io.appwrite.models.Document
import io.appwrite.models.User
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
    private val reviewRepository: ReviewRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val _user = mutableStateOf<User<Map<String, Any>>?>(null)
    val user: State<User<Map<String, Any>>?> = _user

    private val _profileDoc = mutableStateOf<Document<Map<String, Any>>?>(null)
    val profileDoc: State<Document<Map<String, Any>>?> = _profileDoc

    private val _role = mutableStateOf<String?>(null)
    val role: State<String?> = _role

    // Edit states
    private val _isEditing = mutableStateOf(false)
    val isEditing: State<Boolean> = _isEditing

    // Customer fields
    val editName = mutableStateOf("")

    // Artisan fields
    val editPhone = mutableStateOf("")
    val editTradeCategory = mutableStateOf("")
    val editSkills = mutableStateOf("")
    val editServiceArea = mutableStateOf("")
    val editLatitude = mutableStateOf(0.0)
    val editLongitude = mutableStateOf(0.0)

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _reviews = mutableStateOf<List<Review>>(emptyList())
    val reviews: State<List<Review>> = _reviews

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        _isLoading.value = true
        viewModelScope.launch {
            _role.value = dataStoreManager.userRoleFlow.first()
            
            when (val userRes = authRepository.getCurrentUser()) {
                is Resource.Success -> {
                    _user.value = userRes.data
                    val userId = userRes.data?.id ?: return@launch
                    
                    if (_role.value == "artisan") {
                        val profileRes = profileRepository.getArtisanProfile(userId)
                        if (profileRes is Resource.Success && profileRes.data != null) {
                            _profileDoc.value = profileRes.data
                        }
                        // Load this artisan's reviews
                        val reviewsRes = reviewRepository.getReviewsForArtisan(userId)
                        if (reviewsRes is Resource.Success) {
                            _reviews.value = reviewsRes.data ?: emptyList()
                        }
                    } else {
                        val profileRes = profileRepository.getUserProfile(userId)
                        if (profileRes is Resource.Success && profileRes.data != null) {
                            _profileDoc.value = profileRes.data
                        }
                    }
                }
                is Resource.Error -> _uiEvent.emit(UiEvent.ShowSnackbar(userRes.message ?: "Failed to load auth user"))
                else -> Unit
            }
            _isLoading.value = false
        }
    }

    fun startEditing() {
        val docData = _profileDoc.value?.data
        if (docData != null) {
            editName.value = docData["fullName"] as? String ?: _user.value?.name ?: ""
            if (_role.value == "artisan") {
                editPhone.value = docData["phone"] as? String ?: ""
                editTradeCategory.value = docData["tradeCategory"] as? String ?: ""
                editSkills.value = docData["skills"] as? String ?: ""
                editServiceArea.value = docData["serviceArea"] as? String ?: ""
                editLatitude.value = when (val v = docData["latitude"]) {
                    is Double -> v; is Float -> v.toDouble(); is Int -> v.toDouble(); else -> 0.0
                }
                editLongitude.value = when (val v = docData["longitude"]) {
                    is Double -> v; is Float -> v.toDouble(); is Int -> v.toDouble(); else -> 0.0
                }
            }
            _isEditing.value = true
        }
    }

    fun cancelEditing() {
        _isEditing.value = false
    }

    fun saveProfile() {
        val userId = _user.value?.id ?: return
        
        _isLoading.value = true
        viewModelScope.launch {
            if (_role.value == "artisan") {
                val updates = mapOf(
                    "fullName" to editName.value,
                    "phone" to editPhone.value,
                    "tradeCategory" to editTradeCategory.value,
                    "skills" to editSkills.value,
                    "serviceArea" to editServiceArea.value,
                    "latitude" to editLatitude.value,
                    "longitude" to editLongitude.value
                )
                when (val res = profileRepository.updateArtisanProfile(userId, updates)) {
                    is Resource.Success -> {
                        _profileDoc.value = res.data
                        _isEditing.value = false
                        _uiEvent.emit(UiEvent.ShowSnackbar("Profile updated!"))
                    }
                    is Resource.Error -> _uiEvent.emit(UiEvent.ShowSnackbar(res.message ?: "Failed to update profile"))
                    else -> Unit
                }
            } else {
                val updates = mapOf(
                    "fullName" to editName.value
                )
                when (val res = profileRepository.updateUserProfile(userId, updates)) {
                    is Resource.Success -> {
                        _profileDoc.value = res.data
                        _isEditing.value = false
                        _uiEvent.emit(UiEvent.ShowSnackbar("Profile updated!"))
                    }
                    is Resource.Error -> _uiEvent.emit(UiEvent.ShowSnackbar(res.message ?: "Failed to update profile"))
                    else -> Unit
                }
            }
            _isLoading.value = false
        }
    }

    fun logout() {
        _isLoading.value = true
        viewModelScope.launch {
            when (val res = authRepository.logout()) {
                is Resource.Success -> {
                    dataStoreManager.clearRole()
                    _uiEvent.emit(UiEvent.Navigate("login"))
                }
                is Resource.Error -> {
                    _uiEvent.emit(UiEvent.ShowSnackbar(res.message ?: "Failed to logout"))
                }
                else -> Unit
            }
            _isLoading.value = false
        }
    }

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
        data class Navigate(val route: String) : UiEvent()
    }
}
