package com.example.artisanx.notifications

import android.content.Context
import android.util.Log
import com.example.artisanx.ArtisansXFirebaseService
import com.example.artisanx.domain.repository.AuthRepository
import com.example.artisanx.domain.repository.BookingRepository
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
class ChatNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val realtime: Realtime,
    private val authRepository: AuthRepository,
    private val bookingRepository: BookingRepository,
    private val profileRepository: ProfileRepository
) {

    private companion object { const val TAG = "ChatNotifManager" }

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
            val channel = "databases.${Constants.DATABASE_ID}.collections.${Constants.COLLECTION_CHAT_MESSAGES}.documents"
            Log.d(TAG, "Subscribing to '$channel' for user=$userId")
            subscription = try {
                realtime.subscribe(channel) { event ->
                    val isCreate = event.events.any { it.endsWith(".create") }
                    if (!isCreate) return@subscribe
                    @Suppress("UNCHECKED_CAST")
                    val payload = event.payload as? Map<String, Any> ?: return@subscribe
                    val senderId = payload["senderId"] as? String ?: return@subscribe
                    val bookingId = payload["bookingId"] as? String ?: return@subscribe
                    if (senderId == currentUserId) {
                        Log.d(TAG, "  -> skipping own message")
                        return@subscribe
                    }
                    val activeId = ActiveChatTracker.activeBookingId
                    Log.d(TAG, "  -> activeChat=$activeId, eventBooking=$bookingId")
                    if (bookingId == activeId) {
                        Log.d(TAG, "  -> skipping — chat $bookingId is currently open")
                        return@subscribe
                    }
                    val messageText = payload["message"] as? String ?: ""
                    val hasImage = !(payload["imageFileId"] as? String).isNullOrBlank()
                    Log.d(TAG, "  -> dispatching handleNewMessage bookingId=$bookingId from=$senderId")
                    scope.launch { handleNewMessage(bookingId, senderId, messageText, hasImage) }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to subscribe to chat messages: ${e.message}", e)
                null
            }
            Log.d(TAG, "Subscription established: ${subscription != null}")
            startInFlight = false
        }
    }

    private suspend fun handleNewMessage(
        bookingId: String,
        senderId: String,
        message: String,
        hasImage: Boolean
    ) {
        try {
            val bookingRes = bookingRepository.getBookingById(bookingId)
            val booking = (bookingRes as? Resource.Success)?.data
            if (booking == null) {
                Log.w(TAG, "handleNewMessage: booking $bookingId not found")
                return
            }
            val isParticipantCustomer = booking.customerId == currentUserId
            val isParticipantArtisan = booking.artisanId == currentUserId
            if (!isParticipantCustomer && !isParticipantArtisan) {
                Log.d(TAG, "handleNewMessage: skipping — not a participant of $bookingId")
                return
            }

            val senderName = if (isParticipantCustomer) {
                (profileRepository.getArtisanProfile(senderId) as? Resource.Success)
                    ?.data?.data?.get("fullName") as? String ?: "Artisan"
            } else {
                (profileRepository.getUserProfile(senderId) as? Resource.Success)
                    ?.data?.data?.get("fullName") as? String ?: "Customer"
            }

            val body = when {
                hasImage && message.isBlank() -> "Sent a photo"
                hasImage -> "📷 ${message.take(80)}"
                else -> message.take(80)
            }
            Log.i(TAG, "Firing notification: $senderName -> '${body.take(40)}'")
            ArtisansXFirebaseService.showLocalNotification(
                context = context,
                title = senderName,
                body = body,
                bookingId = bookingId
            )
        } catch (e: Exception) {
            Log.e(TAG, "handleNewMessage failed: ${e.message}", e)
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

/**
 * Tracks which chat booking the user is currently viewing, so the
 * [ChatNotificationManager] can skip notifications for that chat.
 *
 * Set on entry (in `ChatViewModel.init`) and cleared on exit (`onCleared`).
 */
object ActiveChatTracker {
    @Volatile var activeBookingId: String? = null
}
