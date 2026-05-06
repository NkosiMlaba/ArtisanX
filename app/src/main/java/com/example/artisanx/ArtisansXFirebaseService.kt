package com.example.artisanx

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ArtisansXFirebaseService : FirebaseMessagingService() {

    companion object {
        const val CHANNEL_ID = "artisanx_channel"
        const val CHANNEL_NAME = "ArtisansX Notifications"
        private var notificationId = 1000

        fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifications for bids, bookings, and messages"
                }
                val manager = context.getSystemService(NotificationManager::class.java)
                manager.createNotificationChannel(channel)
            }
        }

        const val EXTRA_BOOKING_ID = "bookingId"
        const val EXTRA_JOB_ID = "jobId"

        fun showLocalNotification(context: Context, title: String, body: String, bookingId: String? = null) {
            showLocalNotificationInternal(context, title, body, bookingId, null)
        }

        fun showLocalNotificationForJob(context: Context, title: String, body: String, jobId: String) {
            showLocalNotificationInternal(context, title, body, null, jobId)
        }

        private fun showLocalNotificationInternal(
            context: Context,
            title: String,
            body: String,
            bookingId: String?,
            jobId: String?
        ) {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                bookingId?.let { putExtra(EXTRA_BOOKING_ID, it) }
                jobId?.let { putExtra(EXTRA_JOB_ID, it) }
            }
            val pendingIntent = PendingIntent.getActivity(
                context, notificationId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(notificationId++, notification)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Save token to the user's profile in Appwrite so the server can send them notifications
        CoroutineScope(Dispatchers.IO).launch {
            try {
                saveFcmToken(token)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val title = message.notification?.title ?: message.data["title"] ?: "ArtisansX"
        val body = message.notification?.body ?: message.data["body"] ?: ""
        if (body.isNotBlank()) {
            showLocalNotification(this, title, body)
        }
    }

    private suspend fun saveFcmToken(token: String) {
        // Token is saved to DataStore so on next startup the ProfileViewModel can sync it
        val prefs = applicationContext.getSharedPreferences("fcm_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("fcm_token", token).apply()
    }
}
