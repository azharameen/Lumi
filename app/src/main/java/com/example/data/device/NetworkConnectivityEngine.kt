package com.example.data.device

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class NetworkType {
    WIFI, CELLULAR, ETHERNET, OFFLINE
}

data class NetworkStatus(
    val isConnected: Boolean = true,
    val type: NetworkType = NetworkType.WIFI,
    val description: String = "Online & Connected"
)

/**
 * Monitors live device connectivity state (WiFi, Cellular, or Offline) using ConnectivityManager.NetworkCallback.
 */
class NetworkConnectivityEngine(private val context: Context) {

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager

    private val _networkStatus = MutableStateFlow(getCurrentStatus())
    val networkStatus: StateFlow<NetworkStatus> = _networkStatus.asStateFlow()

    private var onStatusChanged: ((NetworkStatus) -> Unit)? = null

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            updateStatus()
        }

        override fun onLost(network: Network) {
            updateStatus()
        }

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            updateStatus()
        }
    }

    fun startListening(onChanged: ((NetworkStatus) -> Unit)? = null) {
        this.onStatusChanged = onChanged
        try {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            connectivityManager?.registerNetworkCallback(request, networkCallback)
            updateStatus()
        } catch (e: Exception) {
            // Ignored
        }
    }

    fun stopListening() {
        try {
            connectivityManager?.unregisterNetworkCallback(networkCallback)
        } catch (e: Exception) {
            // Ignored
        }
    }

    private fun updateStatus() {
        val status = getCurrentStatus()
        _networkStatus.value = status
        onStatusChanged?.invoke(status)
    }

    private fun getCurrentStatus(): NetworkStatus {
        val cm = connectivityManager ?: return NetworkStatus(false, NetworkType.OFFLINE, "Offline Sanctuary")
        val activeNetwork = cm.activeNetwork ?: return NetworkStatus(false, NetworkType.OFFLINE, "Offline Sanctuary")
        val caps = cm.getNetworkCapabilities(activeNetwork) ?: return NetworkStatus(false, NetworkType.OFFLINE, "Offline Sanctuary")

        return when {
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                NetworkStatus(true, NetworkType.WIFI, "WiFi Connected")
            }
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                NetworkStatus(true, NetworkType.CELLULAR, "Cellular Data")
            }
            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                NetworkStatus(true, NetworkType.ETHERNET, "Ethernet Connected")
            }
            else -> {
                NetworkStatus(caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET), NetworkType.OFFLINE, "Offline Sanctuary")
            }
        }
    }
}
