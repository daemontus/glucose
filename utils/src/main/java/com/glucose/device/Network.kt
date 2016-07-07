package com.glucose.device

import android.Manifest
import android.annotation.TargetApi
import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.Build
import android.support.annotation.RequiresPermission

object Network {

    /**
     * @return True if device is currently connected to any network. (Does not guarantee internet access)
     */
    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    fun isConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    /**
     * @return True if device is connected to network that is not metered (not paid by amount of transported data).
     */
    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    fun canDownloadLargeFiles(context: Context): Boolean {
        if (Device.OS.atLeastJellyBean()) {
            return canDownloadLargeFilesNew(context)
        } else {
            return canDownloadLargeFilesLegacy(context)
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    private fun canDownloadLargeFilesNew(context: Context): Boolean {
        return !(context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).isActiveNetworkMetered
    }

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    private fun canDownloadLargeFilesLegacy(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo.type == ConnectivityManager.TYPE_WIFI || activeNetworkInfo.type == ConnectivityManager.TYPE_ETHERNET
    }

    /**
     * @return True if device is connected to wifi network and this connection is active.
     */
    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    fun isWifi(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.type == ConnectivityManager.TYPE_WIFI
    }

    /**
     * Name of currently connected wifi or null if no name is available.
     * WARNING: Requires ACCESS_WIFI_STATE permission.
     */
    @RequiresPermission(Manifest.permission.ACCESS_WIFI_STATE)
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