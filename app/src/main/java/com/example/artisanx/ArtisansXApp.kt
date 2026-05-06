package com.example.artisanx

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.example.artisanx.util.AppwriteImageLoader
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.components.SingletonComponent
import io.appwrite.Client

@HiltAndroidApp
class ArtisansXApp : Application(), ImageLoaderFactory {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface AppwriteClientEntryPoint {
        fun appwriteClient(): Client
    }

    override fun onCreate() {
        super.onCreate()
        ArtisansXFirebaseService.createNotificationChannel(this)
    }

    override fun newImageLoader(): ImageLoader {
        val client = EntryPointAccessors
            .fromApplication(this, AppwriteClientEntryPoint::class.java)
            .appwriteClient()
        return AppwriteImageLoader.create(this, client)
    }
}
