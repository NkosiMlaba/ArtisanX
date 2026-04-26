package com.example.artisanx.util

import kotlin.math.*

object LocationUtils {
    fun haversineKm(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLng / 2).pow(2)
        return r * 2 * atan2(sqrt(a), sqrt(1 - a))
    }

    fun formatDistanceKm(km: Double): String = when {
        km < 1.0 -> "${(km * 1000).toInt()} m"
        km < 10.0 -> String.format("%.1f km", km)
        else -> "${km.toInt()} km"
    }
}
