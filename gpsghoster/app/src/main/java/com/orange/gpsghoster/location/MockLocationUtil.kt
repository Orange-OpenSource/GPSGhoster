/*
 * Software Name: GPSGhoster
 * SPDX-FileCopyrightText: Copyright (c) 2023 Orange
 * SPDX-License-Identifier: BSD 3-Clause "New"
 *
 * This software is distributed under the BSD-3-Clause license.
 *
 * Author: Franck SEROT <frank.serot@orange.com> et al.
*/
package com.orange.gpsghoster.location

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import org.jetbrains.annotations.NotNull
import java.lang.Exception

object MockLocationUtil {
    private var TAG: String = MockLocationUtil::class.java.simpleName

    // ref : http://stackoverflow.com/questions/6880232/disable-check-for-mock-location-prevent-gps-spoofing

    fun hasDevelopmentSetting(context: Context): Boolean {
        var result = false

        if (Build.VERSION.SDK_INT >=  Build.VERSION_CODES.JELLY_BEAN_MR1) { // 17
            result = Settings.Secure.getInt(context.contentResolver,
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED , 0) != 0
        } else if (Build.VERSION.SDK_INT >=  Build.VERSION_CODES.JELLY_BEAN) { // 16
            @Suppress("DEPRECATION")
            result = Settings.Secure.getInt(context.contentResolver,
                Settings.Secure.DEVELOPMENT_SETTINGS_ENABLED , 0) != 0
        }

        return result
    }

    fun hasAccessMockLocation(@NotNull context: Context): Boolean {
        var result = false
        val pm = context.packageManager
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        for (applicationInfo in packages) {
            try {
                val packageInfo = pm.getPackageInfo(
                    applicationInfo.packageName,
                    PackageManager.GET_PERMISSIONS
                )
                if (applicationInfo.packageName == context.packageName) {
                    val requestedPermissions = packageInfo.requestedPermissions
                    if (requestedPermissions != null) {
                        for (index in requestedPermissions.indices) {
                            if ((requestedPermissions[index] == "android.permission.ACCESS_MOCK_LOCATION")) {
                                result = true
                            }
                        }
                    }
                }
            } catch (e: PackageManager.NameNotFoundException) {
                Log.e(TAG, "Got exception " + e.message)
            }
        }

        return result
    }

    fun isMock(@NotNull context: Context, location: Location): Boolean {
        return when {
            Build.VERSION.SDK_INT >=  Build.VERSION_CODES.S -> { // 31
                location.isMock
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 -> { // 18
                @Suppress("DEPRECATION")
                location.isFromMockProvider
            }
            else -> {
                isAllowMockLocation(context)
            }
        }
    }

    // returns true if mock location enabled, false if not enabled.
    fun isAllowMockLocation(@NotNull context: Context): Boolean {
        var result = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {
            result = checkMockLocation(context)
        } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.CUPCAKE && // 3
                Build.VERSION.SDK_INT < Build.VERSION_CODES.M) { // 23
            @Suppress("DEPRECATION")
            result =  Settings.Secure.getInt(context.contentResolver,
                Settings.Secure.ALLOW_MOCK_LOCATION) != 0
        }
        return result
    }

    private fun checkMockLocation(@NotNull context: Context): Boolean {
        var result = true
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val providers = locationManager.allProviders
        for (provider in providers) {
            if (locationManager.isProviderEnabled(provider) && (
                provider.equals(LocationManager.NETWORK_PROVIDER) ||
                provider.equals(LocationManager.GPS_PROVIDER)
            )) {
                try {
                    locationManager.removeTestProvider(provider)
                } catch (exception : Exception) {
                    result = false
                }
            }
        }
        return result
    }
}
