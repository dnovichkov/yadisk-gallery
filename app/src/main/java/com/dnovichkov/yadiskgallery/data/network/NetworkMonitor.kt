package com.dnovichkov.yadiskgallery.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Monitors network connectivity status.
 */
@Singleton
class NetworkMonitor
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        /**
         * Flow that emits detailed connectivity state changes.
         */
        val connectivityState: Flow<ConnectivityState> =
            callbackFlow {
                val callback =
                    object : ConnectivityManager.NetworkCallback() {
                        override fun onAvailable(network: Network) {
                            trySend(getCurrentConnectivityState())
                        }

                        override fun onLost(network: Network) {
                            trySend(getCurrentConnectivityState())
                        }

                        override fun onCapabilitiesChanged(
                            network: Network,
                            networkCapabilities: NetworkCapabilities,
                        ) {
                            trySend(getConnectivityStateFromCapabilities(networkCapabilities))
                        }
                    }

                val request =
                    NetworkRequest.Builder()
                        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        .build()

                connectivityManager.registerNetworkCallback(request, callback)

                // Emit initial state
                trySend(getCurrentConnectivityState())

                awaitClose {
                    connectivityManager.unregisterNetworkCallback(callback)
                }
            }.distinctUntilChanged()

        /**
         * Flow that emits network connectivity status changes.
         */
        val isOnline: Flow<Boolean> =
            connectivityState.map { it.isOnline }

        /**
         * Checks if the device is currently connected to the internet.
         */
        fun isCurrentlyConnected(): Boolean {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        }

        /**
         * Checks if the current connection is Wi-Fi.
         */
        fun isWifiConnected(): Boolean {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

            return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        }

        /**
         * Checks if the current connection is cellular.
         */
        fun isCellularConnected(): Boolean {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

            return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
        }

        /**
         * Gets the current connectivity state.
         */
        fun getCurrentConnectivityState(): ConnectivityState {
            val network =
                connectivityManager.activeNetwork
                    ?: return ConnectivityState.Disconnected
            val capabilities =
                connectivityManager.getNetworkCapabilities(network)
                    ?: return ConnectivityState.Disconnected

            return getConnectivityStateFromCapabilities(capabilities)
        }

        /**
         * Converts network capabilities to ConnectivityState.
         */
        private fun getConnectivityStateFromCapabilities(capabilities: NetworkCapabilities): ConnectivityState {
            val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            val hasValidated = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

            if (!hasInternet || !hasValidated) {
                return ConnectivityState.Disconnected
            }

            val connectionType =
                when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ->
                        ConnectionType.WIFI
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ->
                        ConnectionType.CELLULAR
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ->
                        ConnectionType.ETHERNET
                    else -> ConnectionType.OTHER
                }

            return ConnectivityState.Connected(connectionType)
        }
    }
