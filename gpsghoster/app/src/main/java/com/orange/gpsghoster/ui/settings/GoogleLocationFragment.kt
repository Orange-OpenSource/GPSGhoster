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

import android.os.Bundle
import androidx.preference.*
import com.orange.gpsghoster.R
import com.orange.common.preference.AppPreferenceDataStore

class GoogleLocationFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val appPreferenceDataStore = AppPreferenceDataStore.getInstance(requireContext())

        preferenceManager.preferenceDataStore = appPreferenceDataStore

        setPreferencesFromResource(R.xml.google_location_preferences, rootKey)

        val googleLocation : SwitchPreferenceCompat? = findPreference(getString(R.string.google_location_key))
        if (googleLocation != null) {
            googleLocation.isEnabled = !appPreferenceDataStore.getBooleanField(
                R.string.location_service_key,
                R.string.location_service_default_value)
        }
    }
}
