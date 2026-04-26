package com.example.artisanx.presentation.onboarding

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.artisanx.domain.repository.AuthRepository
import com.example.artisanx.domain.repository.CreditsRepository
import com.example.artisanx.domain.repository.ProfileRepository
import com.example.artisanx.util.AppwriteFileUtils
import com.example.artisanx.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.appwrite.services.Storage
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class OnboardingStep { BASIC, IDENTITY, PORTFOLIO }

@HiltViewModel
class ArtisanOnboardingViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
    private val creditsRepository: CreditsRepository,
    private val storage: Storage
) : ViewModel() {

    private val _currentStep = mutableStateOf(OnboardingStep.BASIC)
    val currentStep: State<OnboardingStep> = _currentStep

    // Step 1 — Basic info
    private val _phone = mutableStateOf("")
    val phone: State<String> = _phone

    private val _tradeCategory = mutableStateOf("")
    val tradeCategory: State<String> = _tradeCategory

    private val _serviceArea = mutableStateOf("")
    val serviceArea: State<String> = _serviceArea

    private val _isStudent = mutableStateOf(false)
    val isStudent: State<Boolean> = _isStudent

    // Step 2 — Student fields
    private val _institutionName = mutableStateOf("")
    val institutionName: State<String> = _institutionName

    private val _studentNumber = mutableStateOf("")
    val studentNumber: State<String> = _studentNumber

    private val _courseField = mutableStateOf("")
    val courseField: State<String> = _courseField

    private val _gradYear = mutableStateOf("")
    val gradYear: State<String> = _gradYear

    private val _studentCardUri = mutableStateOf<Uri?>(null)
    val studentCardUri: State<Uri?> = _studentCardUri

    private val _studentCardUploading = mutableStateOf(false)
    val studentCardUploading: State<Boolean> = _studentCardUploading

    private val _studentCardFileId = mutableStateOf("")

    // Step 2 — Independent fields
    private val _idDocUri = mutableStateOf<Uri?>(null)
    val idDocUri: State<Uri?> = _idDocUri

    private val _idDocUploading = mutableStateOf(false)
    val idDocUploading: State<Boolean> = _idDocUploading

    private val _idFileId = mutableStateOf("")

    private val _certifications = mutableStateOf("")
    val certifications: State<String> = _certifications

    private val _yearsExperience = mutableStateOf("")
    val yearsExperience: State<String> = _yearsExperience

    // Step 3 — Portfolio
    private val _skills = mutableStateOf("")
    val skills: State<String> = _skills

    private val _workPhotoUris = mutableStateListOf<Uri>()
    val workPhotoUris: List<Uri> = _workPhotoUris

    private val _workPhotoUploading = mutableStateOf(false)
    val workPhotoUploading: State<Boolean> = _workPhotoUploading

    private val _uploadedWorkPhotoIds = mutableStateListOf<String>()

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    // Setters
    fun onPhoneChange(v: String) { _phone.value = v }
    fun onTradeCategoryChange(v: String) { _tradeCategory.value = v }
    fun onServiceAreaChange(v: String) { _serviceArea.value = v }
    fun onIsStudentChange(v: Boolean) { _isStudent.value = v }
    fun onInstitutionNameChange(v: String) { _institutionName.value = v }
    fun onStudentNumberChange(v: String) { _studentNumber.value = v }
    fun onCourseFieldChange(v: String) { _courseField.value = v }
    fun onGradYearChange(v: String) { if (v.length <= 4 && v.all { it.isDigit() }) _gradYear.value = v }
    fun onCertificationsChange(v: String) { _certifications.value = v }
    fun onYearsExperienceChange(v: String) { if (v.length <= 2 && v.all { it.isDigit() }) _yearsExperience.value = v }
    fun onSkillsChange(v: String) { if (v.length <= 2000) _skills.value = v }

    fun onStudentCardSelected(uri: Uri) {
        _studentCardUri.value = uri
        _studentCardUploading.value = true
        viewModelScope.launch {
            val fileId = uploadFile(uri, "student_card")
            _studentCardFileId.value = fileId ?: ""
            _studentCardUploading.value = false
            if (fileId == null) {
                _uiEvent.emit(UiEvent.ShowSnackbar("Failed to upload student card"))
            }
        }
    }

    fun onIdDocSelected(uri: Uri) {
        _idDocUri.value = uri
        _idDocUploading.value = true
        viewModelScope.launch {
            val fileId = uploadFile(uri, "id_doc")
            _idFileId.value = fileId ?: ""
            _idDocUploading.value = false
            if (fileId == null) {
                _uiEvent.emit(UiEvent.ShowSnackbar("Failed to upload ID document"))
            }
        }
    }

    fun onWorkPhotoSelected(uri: Uri) {
        if (_workPhotoUris.size >= 3) {
            viewModelScope.launch { _uiEvent.emit(UiEvent.ShowSnackbar("Maximum 3 work photos")) }
            return
        }
        _workPhotoUris.add(uri)
        _workPhotoUploading.value = true
        viewModelScope.launch {
            val fileId = uploadFile(uri, "work_photo")
            if (fileId != null) {
                _uploadedWorkPhotoIds.add(fileId)
            } else {
                _workPhotoUris.remove(uri)
                _uiEvent.emit(UiEvent.ShowSnackbar("Failed to upload work photo"))
            }
            _workPhotoUploading.value = false
        }
    }

    fun removeWorkPhoto(index: Int) {
        if (index < _workPhotoUris.size) _workPhotoUris.removeAt(index)
        if (index < _uploadedWorkPhotoIds.size) _uploadedWorkPhotoIds.removeAt(index)
    }

    private suspend fun uploadFile(uri: Uri, prefix: String): String? =
        AppwriteFileUtils.uploadFromUri(context, storage, uri, prefix)

    fun nextStep() {
        when (_currentStep.value) {
            OnboardingStep.BASIC -> {
                if (_phone.value.isBlank() || _tradeCategory.value.isBlank() || _serviceArea.value.isBlank()) {
                    viewModelScope.launch { _uiEvent.emit(UiEvent.ShowSnackbar("Please fill all fields")) }
                    return
                }
                _currentStep.value = OnboardingStep.IDENTITY
            }
            OnboardingStep.IDENTITY -> {
                val valid = if (_isStudent.value) {
                    _institutionName.value.isNotBlank() && _studentNumber.value.isNotBlank()
                } else {
                    _yearsExperience.value.isNotBlank()
                }
                if (!valid) {
                    viewModelScope.launch { _uiEvent.emit(UiEvent.ShowSnackbar("Please fill required fields")) }
                    return
                }
                _currentStep.value = OnboardingStep.PORTFOLIO
            }
            OnboardingStep.PORTFOLIO -> completeOnboarding()
        }
    }

    fun previousStep() {
        when (_currentStep.value) {
            OnboardingStep.IDENTITY -> _currentStep.value = OnboardingStep.BASIC
            OnboardingStep.PORTFOLIO -> _currentStep.value = OnboardingStep.IDENTITY
            OnboardingStep.BASIC -> {}
        }
    }

    private fun completeOnboarding() {
        if (_skills.value.isBlank()) {
            viewModelScope.launch { _uiEvent.emit(UiEvent.ShowSnackbar("Please describe your skills")) }
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            val userRes = authRepository.getCurrentUser()
            if (userRes is Resource.Success && userRes.data != null) {
                val user = userRes.data
                val profileRes = profileRepository.createArtisanProfile(
                    userId = user.id,
                    fullName = user.name,
                    email = user.email,
                    phone = _phone.value,
                    tradeCategory = _tradeCategory.value,
                    skills = _skills.value,
                    serviceArea = _serviceArea.value,
                    isStudent = _isStudent.value,
                    institutionName = _institutionName.value,
                    studentNumber = _studentNumber.value,
                    studentCardFileId = _studentCardFileId.value,
                    courseField = _courseField.value,
                    gradYear = _gradYear.value.toIntOrNull() ?: 0,
                    idFileId = _idFileId.value,
                    certifications = _certifications.value,
                    yearsExperience = _yearsExperience.value.toIntOrNull() ?: 0,
                    workPhotoIds = _uploadedWorkPhotoIds.toList()
                )

                _isLoading.value = false
                if (profileRes is Resource.Success) {
                    creditsRepository.initializeCredits(user.id, 5)
                    _uiEvent.emit(UiEvent.Navigate("artisan_dashboard"))
                } else {
                    _uiEvent.emit(UiEvent.ShowSnackbar(profileRes.message ?: "Failed to create artisan profile"))
                }
            } else {
                _isLoading.value = false
                _uiEvent.emit(UiEvent.ShowSnackbar("Session expired. Please login again."))
            }
        }
    }

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
        data class Navigate(val route: String) : UiEvent()
    }

    companion object {
        val TRADE_CATEGORIES = listOf(
            "Plumber", "Electrician", "Carpenter", "Painter", "Tiler",
            "Builder / Bricklayer", "Welder", "Mechanic", "HVAC Technician",
            "Landscaper", "Roofer", "Glazier", "Locksmith", "Plasterer",
            "Security Installer", "Solar Installer", "Appliance Repair",
            "General Handyman", "Other"
        )
    }
}
