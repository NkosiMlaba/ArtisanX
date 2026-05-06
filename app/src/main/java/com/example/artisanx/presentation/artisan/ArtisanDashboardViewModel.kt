package com.example.artisanx.presentation.artisan

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.artisanx.domain.model.Bid
import com.example.artisanx.domain.model.Job
import com.example.artisanx.domain.repository.AuthRepository
import com.example.artisanx.domain.repository.BiddingRepository
import com.example.artisanx.domain.repository.CreditsRepository
import com.example.artisanx.domain.repository.JobRepository
import com.example.artisanx.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BidWithJob(val bid: Bid, val job: Job?)

@HiltViewModel
class ArtisanDashboardViewModel @Inject constructor(
    private val creditsRepository: CreditsRepository,
    private val biddingRepository: BiddingRepository,
    private val jobRepository: JobRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _creditBalance = mutableStateOf(0)
    val creditBalance: State<Int> = _creditBalance

    private val _myBids = mutableStateOf<List<BidWithJob>>(emptyList())
    val myBids: State<List<BidWithJob>> = _myBids

    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

    fun loadDashboard() {
        _isLoading.value = true
        viewModelScope.launch {
            val userRes = authRepository.getCurrentUser()
            if (userRes is Resource.Success && userRes.data != null) {
                val userId = userRes.data.id

                when (val creditsRes = creditsRepository.getBalance(userId)) {
                    is Resource.Success -> _creditBalance.value = creditsRes.data ?: 0
                    else -> Unit
                }

                when (val bidsRes = biddingRepository.getBidsByArtisan(userId)) {
                    is Resource.Success -> {
                        val bids = bidsRes.data ?: emptyList()
                        val enriched = bids.map { bid ->
                            val jobRes = jobRepository.getJobById(bid.jobId)
                            BidWithJob(bid, (jobRes as? Resource.Success)?.data)
                        }
                        _myBids.value = enriched
                    }
                    else -> Unit
                }
            }
            _isLoading.value = false
        }
    }
}
