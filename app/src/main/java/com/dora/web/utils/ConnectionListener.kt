package com.dora.web.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL


class ConnectionListener(
    context: Context
){
    enum class State {
        Default,
        HasConnection,
        NoConnection,
        Dismissed
    }

    var isConnected: Boolean = isNetworkAvailable()
    var shouldReactOnChanges: Boolean = true
        set(value) {
            field = value
            if (value && connectionStatusFlow.value != State.Default) {
                connectionStatusFlow.value.let { oldState ->
                    connectionStatusFlow.value = State.Default
                    connectionStatusFlow.value = oldState
                }
            }
        }

    var failureNavigationFlow: MutableSharedFlow<Throwable> = MutableSharedFlow(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    val connectionStatusFlow =
        MutableStateFlow(State.Default)

    private val connectivityManager =
        ContextCompat.getSystemService(context.applicationContext, ConnectivityManager::class.java)

    private val networkRequest = NetworkRequest.Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        }.addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
        .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
        .addTransportType(NetworkCapabilities.TRANSPORT_BLUETOOTH)
        .build()

    private val networkCallback by lazy {
        object : ConnectivityManager.NetworkCallback() {
            override fun onLost(network: Network) {
                "Network lost".log("ConnectionListener")
                updateConnectionStatus()
            }

            override fun onAvailable(network: Network) {
                "Network available".log("ConnectionListener")
                updateConnectionStatus()
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                "Network capabilities changed".log("ConnectionListener")
                updateConnectionStatus()
            }
        }
    }

    fun registerListener() {
        connectivityManager?.registerNetworkCallback(networkRequest, networkCallback)
        updateConnectionStatus()
    }

    fun unregisterListener() {
        try {
            connectivityManager?.unregisterNetworkCallback(networkCallback)
        } catch (ignore: IllegalArgumentException) {
            // Ignore: NetworkCallback was not registered
            "ConnectionListener unregisterListener exception: $ignore".log("ConnectionListener")
        }

        connectionStatusFlow.value = State.Default
    }

    fun setDismissedStatus() {
        connectionStatusFlow.value = State.Dismissed
    }


    @Suppress("DEPRECATION")
    private fun isNetworkAvailable(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager?.activeNetwork ?: return false
            val networkCapabilities =
                connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } else {
            val nwInfo = connectivityManager?.activeNetworkInfo ?: return false
            return nwInfo.isConnected
        }
    }

    private suspend fun hasInternetAccess(): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                val url = URL("https://www.google.com")
                val connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 2000
                connection.connect()
                connection.responseCode == 200
            }
        } catch (_: Exception) {
            false
        }
    }

    private var statusJob : Job? = null
    private val statusScope = CoroutineScope(Dispatchers.IO)

    fun updateConnectionStatus() {
        connectionStatusFlow.value = State.Default
        statusJob?.cancel()
        statusJob = statusScope.launch {
            val networkAvailable = isNetworkAvailable()
            isConnected = networkAvailable
            "ConnectionListener updateConnectionStatus isConnected: $isConnected".log("ConnectionListener")
            connectionStatusFlow.value = if (isConnected) {
                State.HasConnection
            } else {
                State.NoConnection
            }
        }
    }
}
