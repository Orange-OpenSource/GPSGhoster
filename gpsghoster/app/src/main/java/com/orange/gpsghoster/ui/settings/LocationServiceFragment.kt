/*
 * Software Name: GPSGhoster
 * SPDX-FileCopyrightText: Copyright (c) 2023 Orange
 * SPDX-License-Identifier: BSD 3-Clause "New"
 *
 * This software is distributed under the BSD-3-Clause license.
 *
 * Author: Franck SEROT <frank.serot@orange.com> et al.
*/
package com.orange.gpsghoster.ui.settings

import android.app.ActivityManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.*
import com.orange.gpsghoster.R
import com.orange.common.preference.AppPreferenceDataStore
import com.orange.gpsghoster.location.LocationService

class LocationServiceFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.preferenceDataStore =
            AppPreferenceDataStore.getInstance(requireContext())

        setPreferencesFromResource(R.xml.location_service_preferences, rootKey)

        val locationService : SwitchPreferenceCompat? = findPreference(getString(R.string.location_service_key))
        if (locationService != null) {
            locationService.isChecked = isRunningInForeground()
            locationService.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _: Preference, newValue: Any ->
                    if (newValue as Boolean) {
                        startService()
                    } else {
                        stopService()
                    }
                    return@OnPreferenceChangeListener true
                }
        }

    }

    private fun isRunningInForeground(): Boolean {
        var result = false
        val manager = activity?.getSystemService(
            AppCompatActivity.ACTIVITY_SERVICE
        ) as ActivityManager
        @Suppress("DEPRECATION")
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (LocationService::class.java.name == service.service.className) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    if (service.foreground) {
                        result = true
                    }
                } else {
                    result = true
                }
            }
        }
        return result
    }

    private fun startService() {
        Intent(activity, LocationService::class.java).also { intent ->
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                activity?.startForegroundService(intent as Intent?)
            } else {
                activity?.startService(intent as Intent?)
            }
        }
    }

    private fun stopService() {
        Intent(activity, LocationService::class.java).also { intent ->
            activity?.stopService(intent as Intent?)
        }
    }
}
