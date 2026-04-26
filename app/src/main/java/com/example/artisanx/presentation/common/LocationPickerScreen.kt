package com.example.artisanx.presentation.common

import android.Manifest
import android.location.Geocoder
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

data class LocationResult(
    val latitude: Double,
    val longitude: Double,
    val address: String
)

private fun formatCoords(lat: Double, lng: Double): String =
    String.format(Locale.US, "%.5f, %.5f", lat, lng)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun LocationPickerScreen(
    initialLocation: LatLng = LatLng(-29.8587, 31.0218), // Durban, SA default
    onLocationSelected: (LocationResult) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val scope = rememberCoroutineScope()

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialLocation, 14f)
    }

    val markerPosition by remember {
        derivedStateOf { cameraPositionState.position.target }
    }

    // Reverse-geocode the center position after the camera settles
    var displayAddress by remember { mutableStateOf(formatCoords(initialLocation.latitude, initialLocation.longitude)) }
    var isGeocoding by remember { mutableStateOf(false) }

    // Debounce: wait 600ms after camera stops moving, then geocode
    LaunchedEffect(markerPosition) {
        isGeocoding = true
        delay(600)
        val lat = markerPosition.latitude
        val lng = markerPosition.longitude
        displayAddress = reverseGeocode(context, lat, lng) ?: formatCoords(lat, lng)
        isGeocoding = false
    }

    LaunchedEffect(Unit) {
        if (!locationPermission.status.isGranted) {
            locationPermission.launchPermissionRequest()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pin Your Location") },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            val pos = markerPosition
                            onLocationSelected(
                                LocationResult(
                                    latitude = pos.latitude,
                                    longitude = pos.longitude,
                                    address = displayAddress
                                )
                            )
                        },
                        enabled = !isGeocoding
                    ) {
                        Text("Confirm", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = locationPermission.status.isGranted
                ),
                uiSettings = MapUiSettings(
                    myLocationButtonEnabled = locationPermission.status.isGranted,
                    zoomControlsEnabled = true
                )
            )

            // Fixed center pin (moves with camera)
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Selected location",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(48.dp)
                    .offset(y = (-24).dp)
            )

            // Address display at bottom
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Drag the map to position the pin on your location",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    if (isGeocoding) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    } else {
                        Text(
                            text = displayAddress,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

private suspend fun reverseGeocode(context: android.content.Context, lat: Double, lng: Double): String? {
    return try {
        val geocoder = Geocoder(context, Locale.getDefault())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            kotlinx.coroutines.suspendCancellableCoroutine { cont ->
                geocoder.getFromLocation(lat, lng, 1) { addresses ->
                    val addr = addresses.firstOrNull()
                    if (cont.isActive) cont.resume(addr?.let { buildAddressString(it) }, onCancellation = null)
                }
            }
        } else {
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            addresses?.firstOrNull()?.let { buildAddressString(it) }
        }
    } catch (e: Exception) {
        null
    }
}

private fun buildAddressString(address: android.location.Address): String {
    val parts = mutableListOf<String>()
    address.thoroughfare?.let { parts.add(it) }
    address.subLocality?.let { parts.add(it) }
    address.locality?.let { parts.add(it) }
    address.adminArea?.let { parts.add(it) }
    return if (parts.isNotEmpty()) parts.joinToString(", ") else address.getAddressLine(0) ?: ""
}
