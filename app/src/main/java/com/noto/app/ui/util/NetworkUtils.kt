package com.noto.app.ui.util

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

fun ConnectivityManager.isNetworkAvailableAsFlow() = callbackFlow {
    val request = NetworkRequest.Builder().build()
    val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            trySend(true)
        }

        override fun onLost(network: Network) {
            trySend(false)
        }

        override fun onUnavailable() {
            trySend(false)
        }
    }
    registerNetworkCallback(request, callback)
    awaitClose { unregisterNetworkCallback(callback) }
}.distinctUntilChanged()