package com.dnovichkov.yadiskgallery.data.network

/**
 * Represents the current network connectivity state.
 */
sealed class ConnectivityState {
    /**
     * Device is connected to the internet.
     */
    data class Connected(val connectionType: ConnectionType) : ConnectivityState()

    /**
     * Device is disconnected from the internet.
     */
    data object Disconnected : ConnectivityState()

    /**
     * Connectivity state is being determined.
     */
    data object Unknown : ConnectivityState()

    /**
     * Checks if device is online.
     */
    val isOnline: Boolean
        get() = this is Connected

    /**
     * Checks if device is offline.
     */
    val isOffline: Boolean
        get() = this is Disconnected
}

/**
 * Type of network connection.
 */
enum class ConnectionType {
    WIFI,
    CELLULAR,
    ETHERNET,
    OTHER,
}
