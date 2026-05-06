package com.example.artisanx.notifications

import android.content.Context
import android.util.Log
import com.example.artisanx.ArtisansXFirebaseService
import com.example.artisanx.domain.repository.AuthRepository
import com.example.artisanx.domain.repository.JobRepository
import com.example.artisanx.domain.repository.ProfileRepository
import com.example.artisanx.util.Constants
import com.example.artisanx.util.Resource
import dagger.hilt.android.qualifiers.ApplicationContext
import io.appwrite.services.Realtime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.Closeable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BidNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val realtime: Realtime,
    private val authRepository: AuthRepository,
    private val jobRepository: JobRepository,
    private val profileRepository: ProfileRepository
) {

    private companion object { const val TAG = "BidNotifManager" }

    private var subscription: Closeable? = null
    private var currentUserId: String = ""
    private var startInFlight = false

    fun start(scope: CoroutineScope) {
        if (startInFlight || subscription != null) {
            Log.d(TAG, "start() skipped — already subscribed/starting for user=$currentUserId")
            return
        }
        startInFlight = true
        scope.launch {
            val userRes = authRepository.getCurrentUser()
            val userId = (userRes as? Resource.Success)?.data?.id
            if (userId.isNullOrBlank()) {
                Log.d(TAG, "start() skipped — no logged-in user")
                return@launch
            }
            currentUserId = userId
            val channel = "databases.${Constants.DATABASE_ID}.collections.${Constants.COLLECTION_BIDS}.documents"
            Log.d(TAG, "Subscribing to '$channel' for user=$userId")
            subscription = try {
                realtime.subscribe(channel) { event ->
                    Log.d(TAG, "bid event received: events=${event.events}")
                    val isCreate = event.events.any { it.endsWith(".create") }
                    if (!isCreate) {
                        Log.d(TAG, "  -> skipping non-create event")
                        return@subscribe
                    }
                    @Suppress("UNCHECKED_CAST")
                    val payload = event.payload as? Map<String, Any>
                    if (payload == null) {
                        Log.w(TAG, "  -> payload not a Map: ${event.payload?.javaClass?.simpleName}")
                        return@subscribe
                    }
                    val bidArtisanId = payload["artisanId"] as? String ?: return@subscribe
                    val jobId = payload["jobId"] as? String ?: return@subscribe
                    if (bidArtisanId == currentUserId) {
                        Log.d(TAG, "  -> skipping own bid (artisanId == currentUser)")
                        return@subscribe
                    }
                    val priceOffer = (payload["priceOffer"] as? Number)?.toDouble() ?: 0.0
                    Log.d(TAG, "  -> dispatching handleNewBid jobId=$jobId artisan=$bidArtisanId price=$priceOffer")
                    scope.launch { handleNewBid(jobId, bidArtisanId, priceOffer) }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to subscribe to bids channel: ${e.message}", e)
                null
            }
            Log.d(TAG, "Subscription established: ${subscription != null}")
            startInFlight = false
        }
    }

    private suspend fun handleNewBid(jobId: String, artisanId: String, priceOffer: Double) {
        try {
            val jobRes = jobRepository.getJobById(jobId)
            val job = (jobRes as? Resource.Success)?.data
            if (job == null) {
                Log.w(TAG, "handleNewBid: job $jobId not found")
                return
            }
            if (job.customerId != currentUserId) {
                Log.d(TAG, "handleNewBid: skipping — job.customerId=${job.customerId} != currentUser=$currentUserId")
                return
            }

            val profileRes = profileRepository.getArtisanProfile(artisanId)
            val artisanName = (profileRes as? Resource.Success)?.data?.data?.get("fullName") as? String
                ?: "An artisan"

            val priceText = if (priceOffer > 0) "R${priceOffer.toInt()}" else "a price"
            Log.i(TAG, "Firing notification: '${job.title}' from $artisanName ($priceText)")
            ArtisansXFirebaseService.showLocalNotificationForJob(
                context = context,
                title = "New bid on '${job.title}'",
                body = "$artisanName offered $priceText",
                jobId = jobId
            )
        } catch (e: Exception) {
            Log.e(TAG, "handleNewBid failed: ${e.message}", e)
        }
    }

    fun stop() {
        try { subscription?.close() } catch (_: Exception) {}
        subscription = null
        currentUserId = ""
        startInFlight = false
    }
}
