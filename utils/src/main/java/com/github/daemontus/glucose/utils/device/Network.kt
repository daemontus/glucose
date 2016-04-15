package com.github.daemontus.glucose.utils.device

import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiManager

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
        return !(context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).isActiveNetworkMetered
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