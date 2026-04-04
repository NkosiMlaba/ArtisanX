package com.example.artisanx.presentation.artisan

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.artisanx.domain.model.Bid
import com.example.artisanx.domain.repository.AuthRepository
import com.example.artisanx.domain.repository.BiddingRepository
import com.example.artisanx.domain.repository.CreditsRepository
import com.example.artisanx.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArtisanDashboardViewModel @Inject constructor(
    private val creditsRepository: CreditsRepository,
    private val biddingRepository: BiddingRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _creditBalance = mutableStateOf(0)
    val creditBalance: State<Int> = _creditBalance

    private val _myBids = mutableStateOf<List<Bid>>(emptyList())
    val myBids: State<List<Bid>> = _myBids

    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

    fun loadDashboard() {
        _isLoading.value = true
        viewModelScope.launch {
            val userRes = authRepository.getCurrentUser()
            if (userRes is Resource.Success && userRes.data != null) {
                val userId = userRes.data.id

                // Load credits
                when (val creditsRes = creditsRepository.getBalance(userId)) {
                    is Resource.Success -> _creditBalance.value = creditsRes.data ?: 0
                    else -> Unit
                }

                // Load recent bids
                when (val bidsRes = biddingRepository.getBidsByArtisan(userId)) {
                    is Resource.Success -> _myBids.value = bidsRes.data ?: emptyList()
                    else -> Unit
                }
            }
            _isLoading.value = false
        }
    }
}
