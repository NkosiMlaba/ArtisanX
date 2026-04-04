package com.example.artisanx.domain.repository

import com.example.artisanx.domain.model.Booking
import com.example.artisanx.util.Resource

interface BookingRepository {
    suspend fun createBooking(
        jobId: String,
        customerId: String,
        artisanId: String
    ): Resource<Booking>

    suspend fun getBookingsForCustomer(customerId: String): Resource<List<Booking>>

    suspend fun getBookingsForArtisan(artisanId: String): Resource<List<Booking>>

    suspend fun getBookingById(bookingId: String): Resource<Booking>

    suspend fun updateBookingStatus(bookingId: String, status: String): Resource<Booking>

    suspend fun markAsPaid(bookingId: String): Resource<Booking>
}
