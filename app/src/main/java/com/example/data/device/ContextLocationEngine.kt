package com.example.data.device

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class LocationContext(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val approximatePlace: String = "Cozy Sanctuary",
    val hasPermission: Boolean = false
)

/**
 * Google Play Services FusedLocationProviderClient integration
 * Provides real-time coordinates and contextual area detection for Lumi.
 */
class ContextLocationEngine(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val _locationState = MutableStateFlow(LocationContext())
    val locationState: StateFlow<LocationContext> = _locationState.asStateFlow()

    private var locationCallback: LocationCallback? = null

    fun checkPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    fun startLocationUpdates(onLocationReceived: ((Double, Double) -> Unit)? = null) {
        if (!checkPermission()) {
            _locationState.value = _locationState.value.copy(hasPermission = false)
            return
        }

        _locationState.value = _locationState.value.copy(hasPermission = true)

        try {
            // Get last known location immediately
            fusedLocationClient.lastLocation.addOnSuccessListener { loc: Location? ->
                if (loc != null) {
                    updateLocation(loc)
                    onLocationReceived?.invoke(loc.latitude, loc.longitude)
                }
            }

            // Periodic updates
            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                15 * 60 * 1000L // 15 mins
            ).setMinUpdateIntervalMillis(5 * 60 * 1000L)
                .build()

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    result.lastLocation?.let { loc ->
                        updateLocation(loc)
                        onLocationReceived?.invoke(loc.latitude, loc.longitude)
                    }
                }
            }

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            // Handled
        }
    }

    private fun updateLocation(loc: Location) {
        var placeDescription = "Earth Sanctuary"
        try {
            val geocoder = android.location.Geocoder(context, java.util.Locale.getDefault())
            val addresses = geocoder.getFromLocation(loc.latitude, loc.longitude, 1)
            val city = addresses?.firstOrNull()?.locality 
                ?: addresses?.firstOrNull()?.subAdminArea 
                ?: addresses?.firstOrNull()?.adminArea
            if (!city.isNullOrBlank()) {
                placeDescription = city
            }
        } catch (e: Exception) {
            placeDescription = if (loc.latitude != 0.0) {
                "${"%.2f".format(loc.latitude)}, ${"%.2f".format(loc.longitude)}"
            } else {
                "Earth Sanctuary"
            }
        }

        _locationState.value = LocationContext(
            latitude = loc.latitude,
            longitude = loc.longitude,
            approximatePlace = placeDescription,
            hasPermission = true
        )
    }

    fun stopLocationUpdates() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }
    }
}
