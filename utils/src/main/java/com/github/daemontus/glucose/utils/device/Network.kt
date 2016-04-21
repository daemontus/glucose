package com.github.daemontus.glucose.utils.device

import android.annotation.TargetApi
import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.Build

object Network {

    /**
     * @return True if device is currently connected to any network. (Does not guarantee internet access)
     */
    fun isConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    /**
     * @return True if device is connected to network that is not metered (not paid by amount of transported data).
     */
    fun canDownloadLargeFiles(context: Context): Boolean {
        if (Device.OS.atLeastJellyBean()) {
            return canDownloadLargeFilesNew(context)
        } else {
            return canDownloadLargeFilesLegacy(context)
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun canDownloadLargeFilesNew(context: Context): Boolean {
        return !(context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).isActiveNetworkMetered
    }

    private fun canDownloadLargeFilesLegacy(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo.type == ConnectivityManager.TYPE_WIFI || activeNetworkInfo.type == ConnectivityManager.TYPE_ETHERNET
    }

    /**
     * @return True if device is connected to wifi network and this connection is active.
     */
    fun isWifi(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.type == ConnectivityManager.TYPE_WIFI
    }

    /**
     * Name of currently connected wifi or null if no name is available.
     * WARNING: Requires ACCESS_WIFI_STATE permission.
     */
    fun getWifiName(context: Context): String? {
        if (!isWifi(context)) return null
        val wifiInfo = (context.getSystemService(Context.WIFI_SERVICE) as WifiManager).connectionInfo
        if (wifiInfo != null && wifiInfo.ssid != null) {
            var name: String = wifiInfo.ssid
            //some devices tend to add some extra data to the string. Get rid of it.
            if (name.startsWith("\"") && name.endsWith("\"")) name = name.substring(1, name.length - 1)
            return name
        }
        return null
    }

}