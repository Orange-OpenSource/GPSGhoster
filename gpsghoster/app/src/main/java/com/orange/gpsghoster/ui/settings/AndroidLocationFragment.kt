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

class AndroidLocationFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val appPreferenceDataStore = AppPreferenceDataStore.getInstance(requireContext())

        preferenceManager.preferenceDataStore = appPreferenceDataStore

        setPreferencesFromResource(R.xml.android_location_preferences, rootKey)

        val criteria : SwitchPreferenceCompat? = findPreference(getString(R.string.criteria_key))
        if (criteria != null) {
            criteria.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _: Preference, newValue: Any ->
                    updateCriteria(newValue as Boolean)
                    return@OnPreferenceChangeListener true
                }
            updateCriteria(criteria.isChecked)
        }

        val androidLocation : SwitchPreferenceCompat? = findPreference(getString(R.string.android_location_key))
        if (androidLocation != null) {
            androidLocation.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _: Preference, newValue: Any ->
                    if (criteria != null) {
                        updateCriteria((newValue as Boolean) && criteria.isChecked)
                    }
                    return@OnPreferenceChangeListener true
                }
        }

        val locationServiceIsRunning = appPreferenceDataStore.getBooleanField(
            R.string.location_service_key,
            R.string.location_service_default_value)
        if (locationServiceIsRunning) {
            if (androidLocation != null) {
                androidLocation.isEnabled = false
            }
            if (criteria != null) {
                criteria.isEnabled = false
                updateCriteria(false)
            }
        }
    }

    private fun updateCriteria(status : Boolean) {
        findPreference<ListPreference>(getString(R.string.android_location_accuracy_key))?.isEnabled = status
        findPreference<ListPreference>(getString(R.string.android_location_horizontal_accuracy_key))?.isEnabled = status
        findPreference<SwitchPreferenceCompat>(getString(R.string.android_location_is_altitude_required_key))?.isEnabled = status
        findPreference<ListPreference>(getString(R.string.android_location_vertical_accuracy_key))?.isEnabled = status
        findPreference<SwitchPreferenceCompat>(getString(R.string.android_location_is_bearing_required_key))?.isEnabled = status
        findPreference<ListPreference>(getString(R.string.android_location_bearing_accuracy_key))?.isEnabled = status
        findPreference<SwitchPreferenceCompat>(getString(R.string.android_location_is_cost_allowed_key))?.isEnabled = status
        findPreference<ListPreference>(getString(R.string.android_location_power_requirement_key))?.isEnabled = status
        findPreference<SwitchPreferenceCompat>(getString(R.string.android_location_is_speed_required_key))?.isEnabled = status
        findPreference<ListPreference>(getString(R.string.android_location_speed_accuracy_key))?.isEnabled = status
    }
}
