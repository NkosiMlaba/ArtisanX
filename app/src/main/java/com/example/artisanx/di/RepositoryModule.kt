package com.example.artisanx.di

import com.example.artisanx.data.repository.AuthRepositoryImpl
import com.example.artisanx.data.repository.BiddingRepositoryImpl
import com.example.artisanx.data.repository.BookingRepositoryImpl
import com.example.artisanx.data.repository.ChatRepositoryImpl
import com.example.artisanx.data.repository.CreditsRepositoryImpl
import com.example.artisanx.data.repository.JobRepositoryImpl
import com.example.artisanx.data.repository.ProfileRepositoryImpl
import com.example.artisanx.data.repository.ReviewRepositoryImpl
import com.example.artisanx.domain.repository.AuthRepository
import com.example.artisanx.domain.repository.BiddingRepository
import com.example.artisanx.domain.repository.BookingRepository
import com.example.artisanx.domain.repository.ChatRepository
import com.example.artisanx.domain.repository.CreditsRepository
import com.example.artisanx.domain.repository.JobRepository
import com.example.artisanx.domain.repository.ProfileRepository
import com.example.artisanx.domain.repository.ReviewRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindJobRepository(
        jobRepositoryImpl: JobRepositoryImpl
    ): JobRepository

    @Binds
    @Singleton
    abstract fun bindProfileRepository(
        profileRepositoryImpl: ProfileRepositoryImpl
    ): ProfileRepository

    @Binds
    @Singleton
    abstract fun bindBiddingRepository(
        biddingRepositoryImpl: BiddingRepositoryImpl
    ): BiddingRepository

    @Binds
    @Singleton
    abstract fun bindBookingRepository(
        bookingRepositoryImpl: BookingRepositoryImpl
    ): BookingRepository

    @Binds
    @Singleton
    abstract fun bindCreditsRepository(
        creditsRepositoryImpl: CreditsRepositoryImpl
    ): CreditsRepository

    @Binds
    @Singleton
    abstract fun bindChatRepository(
        chatRepositoryImpl: ChatRepositoryImpl
    ): ChatRepository

    @Binds
    @Singleton
    abstract fun bindReviewRepository(
        reviewRepositoryImpl: ReviewRepositoryImpl
    ): ReviewRepository
}
