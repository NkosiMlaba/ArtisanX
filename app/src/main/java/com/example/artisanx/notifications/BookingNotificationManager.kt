package com.example.artisanx.notifications

import android.content.Context
import android.util.Log
import com.example.artisanx.ArtisansXFirebaseService
import com.example.artisanx.domain.repository.AuthRepository
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
class BookingNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val realtime: Realtime,
    private val authRepository: AuthRepository
) {

    private companion object { const val TAG = "BookingNotifManager" }

    private var subscription: Closeable? = null
    private var currentUserId: String = ""
    private var startInFlight = false

    fun start(scope: CoroutineScope) {
        if (startInFlight) return
        startInFlight = true
        scope.launch {
            val userRes = authRepository.getCurrentUser()
            val userId = (userRes as? Resource.Success)?.data?.id
            if (userId.isNullOrBlank()) {
                Log.d(TAG, "start() — no logged-in user; ensuring no stale subscription")
                closeSubscription()
                currentUserId = ""
                startInFlight = false
                return@launch
            }
            if (subscription != null && currentUserId == userId) {
                Log.d(TAG, "start() — already subscribed for user=$userId")
                startInFlight = false
                return@launch
            }
            if (subscription != null && currentUserId != userId) {
                Log.i(TAG, "start() — user changed from '$currentUserId' to '$userId'; resetting subscription")
                closeSubscription()
            }
            currentUserId = userId
            val channel = "databases.${Constants.DATABASE_ID}.collections.${Constants.COLLECTION_BOOKINGS}.documents"
            Log.d(TAG, "Subscribing to '$channel' for user=$userId")
            subscription = try {
                realtime.subscribe(channel) { event ->
                    val isUpdate = event.events.any { it.endsWith(".update") }
                    val isCreate = event.events.any { it.endsWith(".create") }
                    if (!isUpdate && !isCreate) return@subscribe
                    @Suppress("UNCHECKED_CAST")
                    val payload = event.payload as? Map<String, Any> ?: return@subscribe
                    val bookingId = payload["\$id"] as? String ?: payload["bookingId"] as? String
                    val customerId = payload["customerId"] as? String ?: return@subscribe
                    val artisanId = payload["artisanId"] as? String ?: return@subscribe
                    val status = payload["status"] as? String ?: return@subscribe

                    val isCustomer = customerId == currentUserId
                    val isArtisan = artisanId == currentUserId
                    if (!isCustomer && !isArtisan) return@subscribe

                    val (notifTitle, notifBody) = pickNotification(
                        status = status,
                        isCreate = isCreate,
                        isCustomer = isCustomer
                    ) ?: run {
                        Log.d(TAG, "  -> no notification for status=$status (isCreate=$isCreate, isCustomer=$isCustomer)")
                        return@subscribe
                    }

                    Log.i(TAG, "Firing booking notification: '$notifTitle' for ${if (isCustomer) "customer" else "artisan"} (booking=$bookingId, status=$status)")
                    ArtisansXFirebaseService.showLocalNotification(
                        context = context,
                        title = notifTitle,
                        body = notifBody,
                        bookingId = bookingId
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to subscribe to bookings channel: ${e.message}", e)
                null
            }
            Log.d(TAG, "Subscription established: ${subscription != null}")
            startInFlight = false
        }
    }

    /**
     * Returns a (title, body) pair if the current user should be notified
     * for this booking event, or null to skip.
     *
     * The implicit rule: status transitions are made by one party; the
     * notification is rendered for the *other* party only.
     */
    private fun pickNotification(
        status: String,
        isCreate: Boolean,
        isCustomer: Boolean
    ): Pair<String, String>? {
        return when {
            // Booking just created (status=requested) — artisan needs to know
            isCreate && status == "requested" && !isCustomer ->
                "New Booking Request" to "A customer accepted your bid. Tap to view."

            // Artisan moves the booking forward — customer is the recipient
            !isCreate && status == "accepted" && isCustomer ->
                "Booking Accepted" to "The artisan accepted your booking. They'll be in touch soon."
            !isCreate && status == "in_progress" && isCustomer ->
                "Job Started" to "The artisan has started work on your job."
            !isCreate && status == "completed" && isCustomer ->
                "Job Completed" to "The job has been marked as complete. Please leave a review!"

            // Customer cancels — artisan is the recipient
            !isCreate && status == "cancelled" && !isCustomer ->
                "Booking Cancelled" to "The customer cancelled the booking."

            else -> null
        }
    }

    fun stop() {
        closeSubscription()
        currentUserId = ""
        startInFlight = false
    }

    private fun closeSubscription() {
        try { subscription?.close() } catch (_: Exception) {}
        subscription = null
    }
}
