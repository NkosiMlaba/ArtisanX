package com.example.artisanx.util

import android.content.Context
import android.util.Log
import coil.ImageLoader
import io.appwrite.Client
import okhttp3.CookieJar
import okhttp3.OkHttpClient

object AppwriteImageLoader {

    private const val TAG = "ArtisanXImageLoader"

    fun create(context: Context, client: Client): ImageLoader {
        val cookieJar = extractCookieJar(client)
        val builder = OkHttpClient.Builder()
        if (cookieJar != null) {
            builder.cookieJar(cookieJar)
            Log.d(TAG, "Coil configured with Appwrite session cookie jar")
        } else {
            Log.w(TAG, "Could not access Appwrite cookieJar; image requests will be unauthenticated")
        }
        val okHttp = builder.build()

        return ImageLoader.Builder(context)
            .okHttpClient(okHttp)
            .crossfade(true)
            .build()
    }

    private fun extractCookieJar(client: Client): CookieJar? {
        return try {
            val field = client.javaClass.getDeclaredField("cookieJar")
            field.isAccessible = true
            field.get(client) as? CookieJar
        } catch (e: Exception) {
            Log.e(TAG, "Reflection on Appwrite Client.cookieJar failed: ${e.message}")
            null
        }
    }
}
