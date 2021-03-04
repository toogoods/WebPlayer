package com.puxin.webplayer.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

object NetworkChangeUtil {

    var networkType = true

    @Suppress("DEPRECATION")
    fun getNetworkState(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            //低版本使用
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo != null && networkInfo.isAvailable
        } else {
            val network = connectivityManager.activeNetwork
            if (network != null) {
                val nc = connectivityManager.getNetworkCapabilities(network)
                return nc?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) ?: false
            }
        }
        return false
    }
}