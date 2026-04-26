package com.example.artisanx.di

import android.content.Context
import com.example.artisanx.data.remote.GroqApiService
import com.example.artisanx.data.remote.OpenRouterApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.appwrite.Client
import io.appwrite.services.Account
import io.appwrite.services.Databases
import io.appwrite.services.Storage
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppwriteClient(@ApplicationContext context: Context): Client {
        return Client(context)
            .setEndpoint(com.example.artisanx.BuildConfig.APPWRITE_ENDPOINT)
            .setProject(com.example.artisanx.BuildConfig.APPWRITE_PROJECT_ID)
    }

    @Provides
    @Singleton
    fun provideAppwriteAccount(client: Client): Account {
        return Account(client)
    }

    @Provides
    @Singleton
    fun provideAppwriteDatabases(client: Client): Databases {
        return Databases(client)
    }

    @Provides
    @Singleton
    fun provideAppwriteStorage(client: Client): Storage {
        return Storage(client)
    }

    @Provides
    @Singleton
    fun provideGroqApiService(): GroqApiService {
        return Retrofit.Builder()
            .baseUrl("https://api.groq.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GroqApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideOpenRouterApiService(): OpenRouterApiService {
        return Retrofit.Builder()
            .baseUrl("https://openrouter.ai/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenRouterApiService::class.java)
    }
}
