package com.example.artisanx

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ArtisansXApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ArtisansXFirebaseService.createNotificationChannel(this)
    }
}
